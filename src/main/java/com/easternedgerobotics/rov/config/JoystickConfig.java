package com.easternedgerobotics.rov.config;

public interface JoystickConfig {
    String heaveAxis();

    String swayAxis();

    String surgeAxis();

    String yawAxis();

    String cameraAMotorForwardButton();

    String cameraAMotorReverseButton();

    String cameraBMotorForwardButton();

    String cameraBMotorReverseButton();

    String toolingMotorForwardButton();

    String toolingMotorReverseButton();

    String motionReverseButton();

    String cameraAVideoFlipButton();

    String cameraBVideoFlipButton();

    String pitchForwardButton();

    String pitchReverseButton();

    float cameraAMotorSpeed();

    float cameraBMotorSpeed();

    float toolingMotorSpeed();

    float pitchSpeed();
}
