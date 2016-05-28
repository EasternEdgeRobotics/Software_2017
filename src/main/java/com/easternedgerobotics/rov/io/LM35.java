package com.easternedgerobotics.rov.io;

public final class LM35 {
    /**
     * Linear + 10-mV/°C scale factor.
     */
    private static final float TEMPERATURE_CELSIUS_SCALAR = 100f;

    /**
     * The ADC this device uses.
     */
    private final ADC channel;

    /**
     * Constructs a new LM35 sensor using the given ADC.
     * @param channel the ADC instance
     */
    public LM35(final ADC channel) {
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
