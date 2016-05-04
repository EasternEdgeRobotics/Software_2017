package com.easternedgerobotics.rov.value;

public class MotorValue implements MutableValueCompanion<Motor> {
    /**
     * Creates a MotorValue with the given values.
     *
     * @param name the name of the motor
     * @param power -100% to 100% power mapped to range -1f to 1f
     * @return a MotorValue
     */
    public static MotorValue create(
        final String name,
        final float power
    ) {
        final Motor motor = new Motor();
        motor.name = name;
        motor.power = power;
        return new MotorValue(motor);
    }

    private final Motor motor;

    MotorValue(final Motor motor) {
        this.motor = motor;
    }

    public final MotorValue setPower(
        final float power
    ) {
        return MotorValue.create(
            motor.name,
            power
        );
    }

    public final String getName() {
        return motor.name;
    }

    public final float getPower() {
        return motor.power;
    }

    @Override
    public final Motor asMutable() {
        return motor;
    }

    @Override
    public final String toString() {
        return String.format(
            "Motor={%1$s, %2$b}",
            motor.name,
            motor.power
        );
    }
}
