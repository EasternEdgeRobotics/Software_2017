package com.easternedgerobotics.rov.config;

public interface RovConfig {
    long maxHeartbeatGap();

    long cpuPollInterval();

    long sensorPollInterval();

    long sleepDuration();

    byte maestroDeviceNumber();

    byte portAftChannel();

    byte starboardAftChannel();

    byte portForeChannel();

    byte starboardForeChannel();

    byte vertAftChannel();

    byte vertForeChannel();

    byte cameraAMotorChannel();

    byte cameraBMotorChannel();

    byte toolingMotorChannel();

    byte lightChannel();

    boolean altImuSa0High();

    int i2cBus();

    long shutdownTimeout();
}
