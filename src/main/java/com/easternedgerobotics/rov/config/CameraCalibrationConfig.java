package com.easternedgerobotics.rov.config;

public interface CameraCalibrationConfig {
    String cameraAName();

    String cameraBName();

    String cameraAImagesDirectory();

    String cameraBImagesDirectory();

    String cameraAPreUndistortedDirectory();

    String cameraBPreUndistortedDirectory();

    int chessboardWidth();

    int chessboardHeight();
}
