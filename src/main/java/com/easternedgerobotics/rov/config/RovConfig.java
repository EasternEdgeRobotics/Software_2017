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

    byte portVertChannel();

    byte starboardVertChannel();

    byte cameraAMotorChannel();

    byte cameraBMotorChannel();

    byte toolingMotorChannel();

    byte lightChannel();

    byte voltageSensor05VChannel();

    byte voltageSensor12VChannel();

    byte voltageSensor48VChannel();

    byte currentSensor05VChannel();

    byte currentSensor12VChannel();

    byte currentSensor48VChannel();

    boolean altImuSa0High();

    int i2cBus();
}
