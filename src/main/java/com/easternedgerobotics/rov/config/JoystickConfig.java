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

    String toolingAMotorForwardButton();

    String toolingAMotorReverseButton();

    String toolingBMotorForwardButton();

    String toolingBMotorReverseButton();

    String toolingCMotorForwardButton();

    String toolingCMotorReverseButton();

    String motionReverseButton();

    String cameraAVideoFlipButton();

    String cameraBVideoFlipButton();

    String pitchForwardButton();

    String pitchReverseButton();

    float cameraAMotorSpeed();

    float cameraBMotorSpeed();

    float toolingAMotorSpeed();

    float toolingBMotorSpeed();

    float toolingCMotorSpeed();

    float pitchSpeed();
}
