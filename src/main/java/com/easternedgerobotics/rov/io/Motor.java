package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.SpeedValue;

/**
 * See the <a href="http://www.dimensionengineering.com/datasheets/Sabertooth2X5RCQuickStart.pdf">Sabertooth 2x5 RC
 * User's Guide</a> for more information.
 */
public final class Motor {
    /**
     * Max forward PWM signal value (in μs) for the connected to the Sabertooth 2x5 motor controller.
     */
    public static final float MAX_FWD = 2500;

    /**
     * Max reverse PWM signal value (in μs) for the connected to the Sabertooth 2x5 motor controller.
     */
    public static final float MAX_REV = 500;

    /**
     * The output PWM device to write speeds using.
     */
    private final PWM device;

    /**
     * Create a Motor device which uses a pololu channel for communication.
     *
     * @param device PWM output to write speeds
     */
    public Motor(final PWM device) {
        this.device = device;
    }

    /**
     * Write the a {@code SpeedValue} to the device.
     *
     * @param value the desired speed.
     */
    public final void write(final SpeedValue value) {
        device.write(value.getSpeed());
    }
}
