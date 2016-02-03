package com.easternedgerobotics.rov.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class Thruster implements ImmutableValueCompanion<ThrusterValue> {
    public String name;

    public float speed;

    public float voltage;

    public float current;

    public float temperature;

    @Override
    public final ThrusterValue asImmutable() {
        return new ThrusterValue(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Thruster thruster = (Thruster) o;
        final float epsilon = 0.0001f;
        final Function<Float, Predicate<Float>> withinReason = a ->
            b -> (Float.compare(a, b) == 0 || Math.abs(a - b) < epsilon);
        return (
               withinReason.apply(current).test(thruster.current)
            && withinReason.apply(speed).test(thruster.speed)
            && withinReason.apply(temperature).test(thruster.temperature)
            && withinReason.apply(voltage).test(thruster.voltage)
            && Objects.equals(name, thruster.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, speed, voltage, current, temperature);
    }
}
