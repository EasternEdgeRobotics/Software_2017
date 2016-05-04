package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotorValue;

import rx.exceptions.Exceptions;

import java.util.concurrent.TimeUnit;

public class Sabertooth2x5 {
    /**
     * Used to set both channels on the Sabertooth2x5 to zero (idle).
     */
    private static final byte DEVICE_ZERO =    (byte) 0;

    /**
     * Max reverse value for channel 1.
     */
    private static final byte CHANNEL1_REVERSE = (byte) 1;

    /**
     * Idle state value for channel 1.
     */
    private static final byte CHANNEL1_ZERO =    (byte) 64;

    /**
     * Max forward value for channel 1.
     */
    private static final byte CHANNEL1_FORWARD = (byte) 127;

    /**
     * Max reverse value for channel 2.
     */
    private static final byte CHANNEL2_REVERSE = (byte) 128;


    /**
     * Idle state value for channel 2.
     */
    private static final byte CHANNEL2_ZERO =    (byte) 192;

    /**
     * Max forward value for channel 2.
     */
    private static final byte CHANNEL2_FORWARD = (byte) 255;

    /**
     * Deadband value for channel power. Used to avoid device jittering.
     */
    private static final float EPSILON = 0.01f;

    /**
     * Time in microseconds for S2 ping to become stable.
     */
    private static final long SETTLING_TIME = 100;

    private final SerialConnection connection;

    private final Pin s2Controller;

    private MotorValue motor1;

    private MotorValue motor2;

    /**
     * Creates a Sabertooth2x5 motor controller using for the given devices.
     *
     * @param eventPublisher source of motor events
     * @param connection serial connection to Sabertooth2x5 device
     * @param s2Controller gpio connection to Sabertooth2x5 device
     * @param motor1 represents low channel on Sabertooth2x5 device
     * @param motor2 represents high channel on Sabertooth2x5 device
     */
    public Sabertooth2x5(
        final EventPublisher eventPublisher,
        final SerialConnection connection,
        final Pin s2Controller,
        final MotorValue motor1,
        final MotorValue motor2
    ) {
        this.connection = connection;
        this.s2Controller = s2Controller;
        this.motor1 = motor1;
        this.motor2 = motor2;

        eventPublisher.valuesOfType(MotorValue.class).subscribe(motorValue -> {
            if (motorValue.getName().equals(Sabertooth2x5.this.motor1.getName())) {
                Sabertooth2x5.this.motor1 = motorValue;
            }
            if (motorValue.getName().equals(Sabertooth2x5.this.motor2.getName())) {
                Sabertooth2x5.this.motor2 = motorValue;
            }
        });
    }

    /**
     * Update the Sabertooth2x5 motor controller.
     *
     *  - Apply a high voltage to S2 pin on the Sabertooth2x5 device using gpio.
     *  - A grace period >= 0.050ms is given for the device to register.
     *  - High and low channel values are written over serial.
     *  - Re-apply low voltage to S2 pin to disable further writes.
     */
    public final void update() {
        s2Controller.setState(true);
        try {
            TimeUnit.MICROSECONDS.sleep(SETTLING_TIME);
        } catch (final InterruptedException e) {
            throw Exceptions.propagate(e);
        }

        if (Math.abs(motor1.getPower()) < EPSILON) {
            connection.write(CHANNEL1_ZERO);
        } else {
            // the percentage distance from -100% to +100%
            final float scale = (1 + motor1.getPower()) / 2;
            connection.write((byte) (CHANNEL1_REVERSE + (CHANNEL1_FORWARD - CHANNEL1_REVERSE) * scale));
        }

        if (Math.abs(motor2.getPower()) < EPSILON) {
            connection.write(CHANNEL2_ZERO);
        } else {
            // the percentage distance from -100% to +100% power
            final float scale = (1 + motor2.getPower()) / 2;
            connection.write((byte) (CHANNEL2_REVERSE + (CHANNEL2_FORWARD - CHANNEL2_REVERSE) * scale));
        }

        s2Controller.setState(false);
    }

    /**
     * Update the Sabertooth2x5 motor controller with zero values.
     *
     *  - Apply a high voltage to S2 pin on the Sabertooth2x5 device using gpio.
     *  - A grace period >= 0.050ms is given for the device to register.
     *  - Global device zero value is written over serial.
     *  - Re-apply low voltage to S2 pin to disable further writes
     */
    public final void updateZero() {
        s2Controller.setState(true);
        try {
            TimeUnit.MICROSECONDS.sleep(SETTLING_TIME);
        } catch (final InterruptedException e) {
            throw Exceptions.propagate(e);
        }
        connection.write(DEVICE_ZERO);
        s2Controller.setState(false);
    }
}
