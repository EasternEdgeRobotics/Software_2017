package com.easternedgerobotics.rov.config;

public interface LaunchConfig {
    String broadcast();

    int defaultBroadcastPort();

    String serialPort();

    int baudRate();
}
