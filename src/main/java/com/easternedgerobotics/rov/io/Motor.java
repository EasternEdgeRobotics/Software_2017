package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.devices.PWM;
import com.easternedgerobotics.rov.value.SpeedValue;

import rx.Observable;

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
     * The latest value from the speed observable.
     */
    private SpeedValue value;

    /**
     * Create a Motor device which uses a pololu channel for communication.
     *
     * @param values speed observable mapped to the pololu channel
     * @param device PWM output to write speeds
     */
    public Motor(final Observable<SpeedValue> values, final PWM device) {
        this.device = device;
        values.subscribe(v -> value = v);
    }

    /**
     * Write the latest {@code SpeedValue} for this motor to the device.
     */
    public final void write() {
        device.write(value.getSpeed());
    }

    /**
     * Write zero to the motor.
     */
    public final void writeZero() {
        device.writeZero();
    }
}
