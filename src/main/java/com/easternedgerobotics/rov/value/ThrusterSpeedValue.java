package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class ThrusterSpeedValue implements MutableValueCompanion<ThrusterSpeed> {
    /**
     * Creates a ThrusterSpeedValue with the given name.
     *
     * @param name the logical name of the thruster
     * @return a ThrusterSpeedValue
     */
    public static ThrusterSpeedValue create(final String name) {
        return create(name, 0);
    }

    /**
     * Creates a ThrusterSpeedValue with the given name and speed.
     *
     * @param name the logical name of the thruster.
     * @param speed the speed of the thruster.
     * @return a ThrusterSpeedValue
     */
    public static ThrusterSpeedValue create(final String name, final float speed) {
        final ThrusterSpeed thruster = new ThrusterSpeed();
        thruster.name = name;
        thruster.speed = speed;
        return new ThrusterSpeedValue(thruster);
    }

    private final ThrusterSpeed thruster;

    ThrusterSpeedValue(final ThrusterSpeed thruster) {
        this.thruster = thruster;
    }

    public final ThrusterSpeedValue setSpeed(
        final float speed
    ) {
        return ThrusterSpeedValue.create(
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
    public final ThrusterSpeed asMutable() {
        return thruster;
    }

    @Override
    public final String toString() {
        return String.format(
            "ThrusterSpeed={%s, %f}",
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
        final ThrusterSpeedValue that = (ThrusterSpeedValue) o;
        return Objects.equals(thruster, that.thruster);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(thruster);
    }
}
