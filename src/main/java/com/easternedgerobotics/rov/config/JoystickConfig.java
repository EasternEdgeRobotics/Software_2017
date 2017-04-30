package com.easternedgerobotics.rov.config;

public interface JoystickConfig {
    int cameraAMotorForwardButton();

    int cameraAMotorReverseButton();

    int cameraBMotorForwardButton();

    int cameraBMotorReverseButton();

    int toolingMotorForwardButton();

    int toolingMotorReverseButton();

    int motionReverseButton();

    int cameraAVideoFlipButton();

    int cameraBVideoFlipButton();

    float motorRotationSpeed();
}
