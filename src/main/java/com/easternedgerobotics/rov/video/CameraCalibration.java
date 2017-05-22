package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_core.TermCriteria;
import org.bytedeco.javacpp.opencv_imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CameraCalibration {
    private final CameraCalibrationConfig config;

    public CameraCalibration(final CameraCalibrationConfig config) {
        this.config = config;
    }

    public CameraCalibrationResult getCameraACalibrationResult() {
        return getCameraCalibrationResult(
            config.cameraAImagesDirectory(),
            config.chessboardWidth(),
            config.chessboardHeight());
    }

    public CameraCalibrationResult getCameraBCalibrationResult() {
        return getCameraCalibrationResult(
            config.cameraBImagesDirectory(),
            config.chessboardWidth(),
            config.chessboardHeight());
    }

    /**
     * Given an image folder and a chessboard board size, return the camera calibration results.
     * Chessboard sizes are the number of inner corners of the board.
     * ie; if the board is 6x5 then it has 5x4 inner corners
     *
     * @param folder the image folder name
     * @param boardWidth the number of inner corners per any two adjacent columns on the chessboard
     * @param boardHeight the number of inner corners per any two adjacent rows on the chessboard
     * @return the camera calibration results.
     */
    private static CameraCalibrationResult getCameraCalibrationResult(
        final String folder,
        final int boardWidth,
        final int boardHeight
    ) {
        final List<String> fileNames = getFolderFileNames(folder);
        final List<String> validNames = new ArrayList<>();
        final Size boardSize = new Size(boardWidth, boardHeight);
        final MatVector objectCornerMatsVect = new MatVector();
        final MatVector imageCornerMatsVect = new MatVector();
        final Mat cameraMatrix = new Mat();
        final Mat distortionCoeffs = new Mat();

        final Size imageSize = evaluateChessboardImages(
            fileNames, validNames, boardSize, objectCornerMatsVect, imageCornerMatsVect);

        if (imageSize == null) {
            throw new RuntimeException("Error: none of the supplied images were valid!");
        }
        /**
         * This method does the bulk of the camera calibration work. It takes two vectors of vectors,
         * objectCornerMatsVect and imageCornerMatsVect, which are of equal length. Each vector in
         * objectCornerMatsVect is associated with the corresponding index in imageCornerMatsVect.
         * Given that each point in the object corners mat is on uniform distance, the method is able to
         * determine the corresponding skew in the image points given. Combining the results of multiple
         * images, and knowing that they are all taken from the same camera, enables the method to determine
         * intrinsic properties of the camera and develop a precise distortion matrix which can be used to
         * transform real-world coordinates into image coordinates. Rotational and translational vecotrs
         * are not required at the moment, so they are left to be garbage collected.
         */
        final double calibrateCameraRmsError = opencv_calib3d.calibrateCamera(
            objectCornerMatsVect,
            imageCornerMatsVect,
            imageSize,
            cameraMatrix,
            distortionCoeffs,
            new MatVector(),
            new MatVector(),
            0,
            new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 30, 0.00001)
        );

        return new CameraCalibrationResult(
            validNames,
            imageSize,
            calibrateCameraRmsError,
            cameraMatrix,
            distortionCoeffs);
    }

    /**
     * Get all of the file names which belong to a folder.
     *
     * @param folder the folder name
     * @return a list of file names
     */
    private static List<String> getFolderFileNames(final String folder) {
        final File[] files = new File(folder).listFiles();
        if (files != null) {
            return Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Take a list of file names and input them into the OpenCV findChessboardCorners method.
     * These images should contain a chessboard of {@param boardSize} inner corners. All valid
     * files will be added to the {@param validNames} list. Similarly all valid object corners
     * and image corners will be added to {@param objectCornerMatsVect} and to
     * {@param imageCornerMatsVect} respectively. This method will return the size of the images
     * supplied in the {@param fileNames} parameter.
     *
     * @param fileNames The image files to use in calibration.  All files must be the same size
     * @param validNames An output list to store the valid names
     * @param boardSize The inner corner size of the chessboard. See {@code createObjectCorners}
     * @param objectCornerMatsVect An output vector to store the valid object corners
     * @param imageCornerMatsVect An output vector to store the valid image corners
     * @return the size of the images or {@code null} if all images invalid.
     */
    private static Size evaluateChessboardImages(
        final List<String> fileNames,
        final List<String> validNames,
        final Size boardSize,
        final MatVector objectCornerMatsVect,
        final MatVector imageCornerMatsVect
    ) {
        validNames.clear();
        objectCornerMatsVect.resize(0);
        imageCornerMatsVect.resize(0);

        final List<Mat> imageCornerMats = new ArrayList<>();
        Size imageSize = null;

        for (final String fileName : fileNames) {
            final Mat image = opencv_imgcodecs.imread(fileName, opencv_imgcodecs.IMREAD_GRAYSCALE);
            final Mat imageCorners = new Mat();

            if (opencv_calib3d.findChessboardCorners(image, boardSize, imageCorners)) {
                imageCornerMats.add(imageCorners);
                validNames.add(fileName);
                // Create imageSize on first valid image
                if (imageSize == null) {
                    imageSize = image.size();
                } else if (imageSize.width() != image.size().width() || imageSize.height() != image.size().height()) {
                    throw new RuntimeException("Error: all images must have the same size!");
                }
            }
        }

        objectCornerMatsVect.resize(imageCornerMats.size());
        imageCornerMatsVect.resize(imageCornerMats.size());
        for (int i = 0; i < imageCornerMats.size(); i++) {
            objectCornerMatsVect.put(i, createObjectCorners(boardSize));
            imageCornerMatsVect.put(i, imageCornerMats.get(i));
        }

        return imageSize;
    }

    /**
     * Create values to represent the logical location of points on a chessboard.
     * These points are the inner corners of the board.
     * ie; if the board is 6x5 then it has 5x4 inner corners
     *
     * @param boardSize the inner corner size
     * @return a matrix representing inner corners
     */
    private static Mat createObjectCorners(final Size boardSize) {
        final Mat objectCornersMat = new Mat(1, boardSize.area(), opencv_core.CV_32FC3);
        final FloatIndexer indexer = objectCornersMat.createIndexer();
        for (int i = 0; i < boardSize.height(); i++) {
            for (int j = 0; j < boardSize.width(); j++) {
                final int index = i * boardSize.width() + j;
                indexer.put(0, index, 0, i);
                indexer.put(0, index, 1, j);
                indexer.put(0, index, 2, 0);
            }
        }
        return objectCornersMat;
    }
}
