package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.pololu.PololuMaestroOutputChannel;
import com.easternedgerobotics.rov.value.SpeedValue;

import rx.Observable;

/**
 * See the <a href="http://www.dimensionengineering.com/datasheets/Sabertooth2X5RCQuickStart.pdf">Sabertooth 2x5 RC
 * User’s Guide</a> for more information.
 */
public final class Motor {

    /**
     * Max forward value for PololuMaestroOutputChannel connected to the Sabertooth 2x5 motor controller.
     */
    public static final float MAX_FWD = 500;

    /**
     * Max reverse value for PololuMaestroOutputChannel connected to the Sabertooth 2x5 motor controller.
     */
    public static final float MAX_REV = 2500;

    /**
     * The output pololu channel to write speeds.
     */
    private final PololuMaestroOutputChannel device;

    /**
     * The latest value from the speed observable.
     */
    private SpeedValue value;

    /**
     * Create a Motor device which uses a pololu channel for communication.
     *
     * @param values speed observable mapped to the pololu channel
     * @param device pololu channel to write speeds.
     */
    public Motor(
        final Observable<SpeedValue> values,
        final PololuMaestroOutputChannel device
    ) {
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
