package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.pololu.PololuMaestroInputChannel;

public class LM35 {
    /**
     * Linear + 10-mV/°C scale factor.
     */
    private static final float TEMPERATURE_CELSIUS_SCALAR = 100f;

    /**
     * The Pololu Maestro channel this device uses.
     */
    private final PololuMaestroInputChannel channel;

    /**
     * Constructs a new LM35 sensor on the given channel.
     * @param channel the channel this device uses
     */
    public LM35(final PololuMaestroInputChannel channel) {
        this.channel = channel;
    }

    /**
     * Returns the temperature.
     * @return the temperature
     */
    public final float read() {
        return channel.voltage() * TEMPERATURE_CELSIUS_SCALAR;
    }
}
