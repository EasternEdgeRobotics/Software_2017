package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.SpeedValue;

import rx.Observable;

public class Light {
    /**
     * Max forward PWM signal value (in μs) for the connected to lighting controller.
     */
    public static final float MAX_FWD = 2000;

    /**
     * Max reverse used to center the stop period at 0%.
     */
    public static final float MAX_REV = 1000;

    /**
     * The output PWM device to write speeds.
     */
    private final PWM device;

    /**
     * The latest value from the speed observable.
     */
    private SpeedValue value;

    /**
     * Create a Lighting device which uses a pololu channel for communication.
     * The Min and Max SpeedValue value for this device is 0 and 1 respectively.
     *
     * @param values speed observable mapped to the pololu channel
     * @param device PWM output to write speeds
     */
    public Light(final Observable<SpeedValue> values, final PWM device) {
        this.device = device;
        values.subscribe(v -> value = v);
    }

    /**
     * Write the latest {@code SpeedValue} for this light.
     */
    public final void write() {
        if (value.getSpeed() < 0 || value.getSpeed() > 1) {
            throw new IllegalArgumentException("Light channel values must be between 0 and 1");
        }
        device.write(value.getSpeed());
    }

    /**
     * Write zero to the light.
     */
    public final void writeZero() {
        device.writeZero();
    }
}
