package com.easternedgerobotics.rov.config;

public interface CameraCalibrationConfig {
    String cameraAImagesDirectory();

    String cameraBImagesDirectory();

    String cameraAValidImagesDirectory();

    String cameraBValidImagesDirectory();

    String cameraAPreUndistortedDirectory();

    String cameraBPreUndistortedDirectory();

    int chessboardWidth();

    int chessboardHeight();
}
