package com.easternedgerobotics.rov.config;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class MockJoystickConfig implements JoystickConfig {
    public int cameraAMotorForwardButton() {
        return 1;
    }

    public int cameraAMotorReverseButton() {
        return 2;
    }

    public int cameraBMotorForwardButton() {
        return 3;
    }

    public int cameraBMotorReverseButton() {
        return 4;
    }

    public int toolingMotorForwardButton() {
        return 5;
    }

    public int toolingMotorReverseButton() {
        return 6;
    }

    public int motionReverseButton() {
        return 7;
    }

    public int cameraAVideoFlipButton() {
        return 8;
    }

    public int cameraBVideoFlipButton() {
        return 9;
    }

    public float motorRotationSpeed() {
        return 0.5f;
    }

}
