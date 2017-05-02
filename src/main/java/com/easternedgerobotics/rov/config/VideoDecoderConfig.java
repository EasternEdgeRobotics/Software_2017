package com.easternedgerobotics.rov.config;

public interface VideoDecoderConfig {
    String host();

    int cameraAVideoPort();

    int cameraBVideoPort();

    String format();

    double frameRate();

    int socketBacklog();
}
