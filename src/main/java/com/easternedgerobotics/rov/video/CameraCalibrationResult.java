package com.easternedgerobotics.rov.video;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;

import java.util.List;

public final class CameraCalibrationResult {
    private final List<String> validFileNames;

    private final Size imageSize;

    /**
     * calibrateCamera returns the root mean square (RMS) re-projection error,
     * usually it should be between 0.1 and 1.0 pixels in a good calibration.
     *
     * See http://stackoverflow.com/a/29897948/3280538
     */
    private final double calibrateCameraRmsError;

    private final Mat cameraMatrix;

    private final Mat distortionCoeffs;

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
    }

    public List<String> getValidFileNames() {
        return validFileNames;
    }

    public Size getImageSize() {
        return imageSize;
    }

    public double getCalibrateCameraRmsError() {
        return calibrateCameraRmsError;
    }

    public Mat getCameraMatrix() {
        return cameraMatrix;
    }

    public Mat getDistortionCoeffs() {
        return distortionCoeffs;
    }
}
