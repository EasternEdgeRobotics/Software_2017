package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class SpeedValue implements MutableValueCompanion<Speed> {
    /**
     * Creates a SpeedValue with the given name.
     *
     * @param name the logical name of the thruster
     * @return a SpeedValue
     */
    public static SpeedValue create(final String name) {
        return create(name, 0);
    }

    /**
     * Creates a SpeedValue with the given name and speed.
     *
     * @param name the logical name of the thruster.
     * @param speed the speed of the thruster.
     * @return a SpeedValue
     */
    public static SpeedValue create(final String name, final float speed) {
        final Speed thruster = new Speed();
        thruster.name = name;
        thruster.speed = speed;
        return new SpeedValue(thruster);
    }

    private final Speed thruster;

    SpeedValue(final Speed thruster) {
        this.thruster = thruster;
    }

    public final SpeedValue setSpeed(
        final float speed
    ) {
        return SpeedValue.create(
            thruster.name,
            speed
        );
    }

    public final String getName() {
        return thruster.name;
    }

    public final float getSpeed() {
        return thruster.speed;
    }

    @Override
    public final Speed asMutable() {
        return thruster;
    }

    @Override
    public final String toString() {
        return String.format(
            "Speed={%s, %f}",
            thruster.name,
            thruster.speed
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SpeedValue that = (SpeedValue) o;
        return Objects.equals(thruster, that.thruster);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(thruster);
    }
}
