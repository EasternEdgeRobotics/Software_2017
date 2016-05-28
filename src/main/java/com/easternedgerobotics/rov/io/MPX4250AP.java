package com.easternedgerobotics.rov.io;

/**
 * A MPX4250 is an absolute pressure (AP) sensor. See also: <a href="http://goo.gl/n7c8tY">MPX4250</a>.
 */
public final class MPX4250AP {
    /**
     * Scalar value for calculating pressure from voltage.
     */
    private static final float PRESSURE_SCALAR = 0.264909785f;

    /**
     * Offset value for calculating pressure from voltage.
     */
    private static final float PRESSURE_OFFSET = 54.20054200f;

    /**
     * The ADC this device uses.
     */
    private final ADC channel;

    /**
     * Constructs a new MPX4250AP sensor on the given channel.
     * @param channel the ADC this device uses
     */
    public MPX4250AP(final ADC channel) {
        this.channel = channel;
    }

    /**
     * Returns the pressure.
     * @return the pressure
     */
    public final float read() {
        return channel.voltage() * PRESSURE_OFFSET - PRESSURE_SCALAR;
    }
}
