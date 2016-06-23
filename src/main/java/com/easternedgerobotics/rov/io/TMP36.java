package com.easternedgerobotics.rov.io;

import rx.Observable;

public final class TMP36 {
    /**
     * Linear + 10-mV/°C scale factor.
     */
    private static final float TEMPERATURE_CELSIUS_SCALAR = 100f;

    private static final float VOLTAGE_OFFSET = 0.5f;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static final Observable.Transformer<Double, Double> CALIBRATION = source -> source.map(
        x -> (
              -0.00000019445948205160 * Math.pow(x, 5)
            +  0.00002993174785553830 * Math.pow(x, 4)
            + -0.00157485910080997000 * Math.pow(x, 3)
            +  0.03450025313268360000 * Math.pow(x, 2)
            +  0.58578793256563300000 * Math.pow(x, 1)
            +  1.29831273496726000000
        ));

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
