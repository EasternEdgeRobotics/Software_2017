package com.easternedgerobotics.rov.config;

public interface CameraCalibrationConfig {
    String cameraAImagesDirectory();

    String cameraBImagesDirectory();

    int chessboardWidth();

    int chessboardHeight();
}
