package com.easternedgerobotics.rov.config;

public interface CameraCalibrationConfig {
    String cameraAImagesDirectory();

    String cameraBImagesDirectory();

    String cameraAValidImagesDirectory();

    String cameraBValidImagesDirectory();

    int chessboardWidth();

    int chessboardHeight();
}
