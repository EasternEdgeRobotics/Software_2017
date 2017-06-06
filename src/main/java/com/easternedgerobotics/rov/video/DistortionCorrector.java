package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.value.CameraCalibrationValue;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class DistortionCorrector {
    /**
     * A mapping of x points to use in image transform.
     */
    private final Mat map1 = new Mat();

    /**
     * A mapping of y points to use in image transform.
     */
    private final Mat map2 = new Mat();

    /**
     * Create an object which is able to undistort an image from a camera
     * given its calibration results.
     *
     * @param value the calibration result for a camera.
     */
    @SuppressWarnings({"checkstyle:trailingcomment"})
    public DistortionCorrector(final CameraCalibrationValue value) {
        opencv_imgproc.initUndistortRectifyMap(
            value.getCameraMatrix(),
            value.getDistortionCoeffs(),
            new Mat(), // optional rectification (none)
            new Mat(), // camera matrix to generate undistorted
            value.getImageSize(),
            opencv_core.CV_32FC1,
            map1,
            map2);
    }

    /**
     * This image will map an input image to a new image based on the
     * the distortion camera calibration results provided to the instances ctor.
     *
     * @param input the input distorted image
     * @param output the output undistorted image
     * @return the output file
     */
    public File apply(
        final File input,
        final File output
    ) {
        try {
            final Mat image = opencv_imgcodecs.imread(input.getAbsolutePath());
            final Mat undistorted = new Mat();
            opencv_imgproc.remap(image, undistorted, map1, map2, opencv_imgproc.INTER_LINEAR);
            final Path temp = File.createTempFile("TcpFileReceiver", ".tmp").toPath();
            opencv_imgcodecs.imwrite(temp.toAbsolutePath().toString(), undistorted);
            Files.copy(temp, output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            Logger.error(e);
        }
        return output;
    }
}
