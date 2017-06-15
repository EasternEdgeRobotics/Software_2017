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

    public byte toolingAMotorChannel() {
        return 0x09;
    }

    public byte toolingBMotorChannel() {
        return 0x0a;
    }

    public byte toolingCMotorChannel() {
        return 0x0b;
    }

    public byte lightAChannel() {
        return 0x0c;
    }

    public byte lightBChannel() {
        return 0x0d;
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

    public String bluetoothComPortName() {
        return "bluetooth";
    }

    public String bluetoothComPort() {
        return "/dev/ttyS0";
    }

    public int bluetoothConnectionTimeout() {
        return 2000;
    }

    public int bluetoothBaudRate() {
        return 9600;
    }

    public int pressureSensorConvertTime() {
        return 9600;
    }
}
