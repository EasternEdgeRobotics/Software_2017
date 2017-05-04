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

    public byte vertAftChannel() {
        return 0x05;
    }

    public byte vertForeChannel() {
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
