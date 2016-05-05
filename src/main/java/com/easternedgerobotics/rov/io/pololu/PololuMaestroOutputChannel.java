package com.easternedgerobotics.rov.io.pololu;

public final class PololuMaestroOutputChannel extends PololuMaestroChannel {
    /**
     * Maximum forward pwm period.
     */
    private final float maxForward;

    /**
     * Maximum reverse pwm period.
     */
    private final float maxReverse;

    /**
     * Create a new PololuMaestroOutputChannel bounded to the maximum forward and reverse pwm values for the device.
     *
     * @param maestro the maestro unit.
     * @param channel the channel used on the maestro device.
     * @param maxForward the maximum clockwise speed.
     * @param maxReverse this maximum counter clockwise speed.
     */
    public PololuMaestroOutputChannel(
        final PololuMaestro maestro,
        final byte channel,
        final float maxForward,
        final float maxReverse
    ) {
        super(maestro, channel);
        this.maxForward = maxForward;
        this.maxReverse = maxReverse;
    }

    /**
     * Write a percent speed to this channel.
     *
     * @param value percent from -1 to 1
     */
    public final void write(final float value) {
        setTarget(rangeMap(value));
    }

    /**
     * Write zero to the channel.
     */
    public final void writeZero() {
        setTarget(rangeMap(0));
    }

    /**
     * Returns the speed value mapped to the input microseconds valid for the PWM channel.
     *
     * @param speed the thruster speed from -1 to 1
     * @return the microseconds for Pulse Width Signal
     */
    private short rangeMap(final float speed) {
        return (short) Math.round(rangeMap(speed, -1f, 1f, maxForward, maxReverse));
    }

    /**
     * Returns the given value mapped from Range A to Range B. See also
     * <a href="https://git.io/vwrID">{@code EasternEdge.Common.Utils.ExtensionMethods.NumberRangeMapExtensions}</a>.
     * @param value the value to map
     * @param fromA the start of the range the value is in now
     * @param fromB the end of the range the value is in now
     * @param toA the start of the range the value is being mapped to
     * @param toB the end of the range the value is being mapped to
     * @return the mapped value
     */
    private float rangeMap(final float value, final float fromA, final float fromB, final float toA, final float toB) {
        return toA + (toB - toA) * (value - fromA) / (fromB - fromA);
    }
}
