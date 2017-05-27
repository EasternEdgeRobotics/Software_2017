package com.easternedgerobotics.rov.video;

import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.Collections;
import java.util.List;

public final class CameraCalibrationResult {
    /**
     * The files used to create the calibration result.
     */
    private final List<String> validFileNames;

    /**
     * The size of the calibrated image.
     */
    private final Size imageSize;

    /**
     * The root mean square (RMS) re-projection error of the calibration
     * usually it should be between 0.1 and 1.0 pixels in a good calibration.
     *
     * See http://stackoverflow.com/a/29897948/3280538
     */
    private final double calibrateCameraRmsError;

    /**
     * This intrinsic camera properties.
     */
    private final Mat cameraMatrix;

    private final double fx;

    private final double fy;

    private final double cx;

    private final double cy;

    /**
     * This distortion coefficients associated with the camera.
     * Turns real world coordinates into image space coordinates.
     */
    private final Mat distortionCoeffs;

    private final double k1;

    private final double k2;

    private final double p1;

    private final double p2;

    private final double k3;

    /**
     * The results from running a camera calibration.
     *
     * @param validFileNames The files used to create the calibration result.
     * @param imageSize The size of the calibrated image.
     * @param calibrateCameraRmsError The root mean square (RMS) re-projection error of the calibration
     * @param cameraMatrix This intrinsic camera matrix.
     * @param distortionCoeffs The distortion coefficients associated with the camera.
     */
    @SuppressWarnings({"checkstyle:magicnumber"})
    public CameraCalibrationResult(
        final List<String> validFileNames,
        final Size imageSize,
        final double calibrateCameraRmsError,
        final Mat cameraMatrix,
        final Mat distortionCoeffs
    ) {
        this.validFileNames = validFileNames;
        this.imageSize = imageSize;
        this.calibrateCameraRmsError = calibrateCameraRmsError;
        this.cameraMatrix = cameraMatrix;
        this.distortionCoeffs = distortionCoeffs;
        final DoubleIndexer cameraMatrixIndexer = cameraMatrix.createIndexer();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(cameraMatrixIndexer.get(i, j) + " ");
            }
            System.out.println();
        }
        fx = cameraMatrixIndexer.get(0, 0);
        fy = cameraMatrixIndexer.get(1, 1);
        cx = cameraMatrixIndexer.get(0, 2);
        cy = cameraMatrixIndexer.get(1, 2);
        final DoubleIndexer distortionCoeffsIndexer = distortionCoeffs.createIndexer();
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(distortionCoeffsIndexer.get(i, j) + " ");
            }
            System.out.println();
        }
        k1 = distortionCoeffsIndexer.get(0, 0);
        k2 = distortionCoeffsIndexer.get(0, 1);
        p1 = distortionCoeffsIndexer.get(0, 2);
        p2 = distortionCoeffsIndexer.get(0, 3);
        k3 = distortionCoeffsIndexer.get(0, 4);
    }

    /**
     * @return The files used to create the calibration result.
     */
    public List<String> getValidFileNames() {
        return Collections.unmodifiableList(validFileNames);
    }

    /**
     * @return The size of the calibrated image.
     */
    public Size getImageSize() {
        return imageSize;
    }

    /**
     * @return The root mean square (RMS) re-projection error of the calibration
     */
    public double getCalibrateCameraRmsError() {
        return calibrateCameraRmsError;
    }

    /**
     * @return This intrinsic camera matrix.
     */
    public Mat getCameraMatrix() {
        return cameraMatrix;
    }

    /**
     * @return The distortion coefficients associated with the camera.
     */
    public Mat getDistortionCoeffs() {
        return distortionCoeffs;
    }
}
