package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.pololu.PololuMaestroInputChannel;

public class MPX4250AP {
    /**
     * Scalar value for calculating pressure from voltage.
     */
    private static final float PRESSURE_SCALAR = 0.264909785f;

    /**
     * Offset value for calculating pressure from voltage.
     */
    private static final float PRESSURE_OFFSET = 54.20054200f;

    /**
     * The Pololu Maestro channel this device uses.
     */
    private final PololuMaestroInputChannel channel;

    /**
     * Constructs a new MPX4250AP sensor on the given channel.
     * @param channel the channel this device uses
     */
    public MPX4250AP(final PololuMaestroInputChannel channel) {
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
