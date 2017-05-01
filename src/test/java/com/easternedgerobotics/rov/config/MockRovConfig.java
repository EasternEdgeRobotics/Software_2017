package com.easternedgerobotics.rov.config;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class MockRovConfig implements RovConfig {
    public long maxHeartbeatGap() {
        return 5;
    }

    public long cpuPollInterval() {
        return 1;
    }

    public long sensorPollInterval() {
        return 10;
    }

    public long sleepDuration() {
        return 100;
    }

    public byte maestroDeviceNumber() {
        return 0x01;
    }

    public byte portAftChannel() {
        return 0x01;
    }

    public byte starboardAftChannel() {
        return 0x02;
    }

    public byte portForeChannel() {
        return 0x03;
    }

    public byte starboardForeChannel() {
        return 0x04;
    }

    public byte portVertChannel() {
        return 0x05;
    }

    public byte starboardVertChannel() {
        return 0x06;
    }

    public byte cameraAMotorChannel() {
        return 0x07;
    }

    public byte cameraBMotorChannel() {
        return 0x08;
    }

    public byte toolingMotorChannel() {
        return 0x09;
    }

    public byte lightChannel() {
        return 0x0a;
    }

    public byte voltageSensor05VChannel() {
        return 0x0b;
    }

    public byte voltageSensor12VChannel() {
        return 0x0c;
    }

    public byte voltageSensor48VChannel() {
        return 0x0d;
    }

    public byte currentSensor05VChannel() {
        return 0x0e;
    }

    public byte currentSensor12VChannel() {
        return 0x0f;
    }

    public byte currentSensor48VChannel() {
        return 0x10;
    }

    public boolean altImuSa0High() {
        return false;
    }

    public int i2cBus() {
        return 1;
    }

    public long shutdownTimeout() {
        return 1;
    }
}
