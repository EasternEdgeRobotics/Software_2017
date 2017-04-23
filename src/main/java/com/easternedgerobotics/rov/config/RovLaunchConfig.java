package com.easternedgerobotics.rov.config;

public interface RovLaunchConfig {
    String broadcast();

    String serialPort();

    int baudRate();
}
