package com.easternedgerobotics.rov.config;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class MockJoystickConfig implements JoystickConfig {
    @Override
    public String heaveAxis() {
        return "slider";
    }

    @Override
    public String swayAxis() {
        return "x";
    }

    @Override
    public String surgeAxis() {
        return "y";
    }

    @Override
    public String yawAxis() {
        return "rz";
    }

    @Override
    public String cameraAMotorForwardButton() {
        return "1";
    }

    @Override
    public String cameraAMotorReverseButton() {
        return "2";
    }

    @Override
    public String cameraBMotorForwardButton() {
        return "3";
    }

    @Override
    public String cameraBMotorReverseButton() {
        return "4";
    }

    @Override
    public String toolingMotorForwardButton() {
        return "5";
    }

    @Override
    public String toolingMotorReverseButton() {
        return "6";
    }

    @Override
    public String motionReverseButton() {
        return "7";
    }

    @Override
    public String cameraAVideoFlipButton() {
        return "8";
    }

    @Override
    public String cameraBVideoFlipButton() {
        return "9";
    }

    @Override
    public String pitchForwardButton() {
        return "10";
    }

    @Override
    public String pitchReverseButton() {
        return "11";
    }

    @Override
    public float cameraAMotorSpeed() {
        return 1;
    }

    @Override
    public float cameraBMotorSpeed() {
        return 1;
    }

    @Override
    public float toolingMotorSpeed() {
        return 1;
    }

    @Override
    public float pitchSpeed() {
        return 1;
    }
}
