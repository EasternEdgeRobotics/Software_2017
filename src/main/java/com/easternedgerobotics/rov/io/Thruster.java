package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.SpeedValue;

/**
 * See the <a href="http://docs.bluerobotics.com/besc/#specification-table">Specification Table for the Blue
 * Robotics Basic ESCs</a> for more information.
 */
public final class Thruster {
    /**
     * Max forward PWM signal value (in μs) for the connected to the Blue Robotics basic ESC.
     */
    public static final float MAX_FWD = 1900;

    /**
     * Max reverse PWM signal value (in μs) for the connected to the Blue Robotics basic ESC.
     */
    public static final float MAX_REV = 1100;

    /**
     * The output PWM device to write speeds.
     */
    private final PWM device;

    /**
     * Create a Thruster device which uses a pololu channel for communication.
     *
     * @param device PWM output to write speeds
     */
    public Thruster(final PWM device) {
        this.device = device;
    }

    /**
     * Write a {@code SpeedValue} to the device.
     *
     * @param value the desired speed value.
     */
    public final void apply(final SpeedValue value) {
        device.write(value.getSpeed());
    }

    /**
     * Write zero to the thruster.
     */
    public final void writeZero() {
        device.writeZero();
    }
}
