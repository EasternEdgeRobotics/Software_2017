package com.easternedgerobotics.rov.config;

public interface VideoDecoderConfig {
    String host();

    int portA();

    int portB();

    String format();

    double frameRate();

    int socketBacklog();

    int bitrate();

    String preset();

    int numBuffers();
}
