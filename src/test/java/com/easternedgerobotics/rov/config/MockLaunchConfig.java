package com.easternedgerobotics.rov.config;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class MockLaunchConfig implements LaunchConfig {
    public String broadcast() {
        return "192.168.88.255";
    }

    public int defaultBroadcastPort() {
        return 10003;
    }

    public String serialPort() {
        return "/dev/ttyACM0";
    }

    public int baudRate() {
        return 115200;
    }
}
