package com.easternedgerobotics.rov.io;

public final class TMP36 {
    /**
     * Linear + 10-mV/°C scale factor.
     */
    private static final float TEMPERATURE_CELSIUS_SCALAR = 100f;

    private static final float VOLTAGE_OFFSET = 0.5f;

    /**
     * The ADC this device uses.
     */
    private final ADC channel;

    /**
     * Constructs a new TMP36 sensor using the given ADC.
     * @param channel the ADC instance
     */
    public TMP36(final ADC channel) {
        this.channel = channel;
    }

    /**
     * Returns the temperature.
     * @return the temperature
     */
    public final float read() {
        return (channel.voltage() - VOLTAGE_OFFSET) * TEMPERATURE_CELSIUS_SCALAR;
    }
}
