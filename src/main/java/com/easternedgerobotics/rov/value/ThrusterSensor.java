package com.easternedgerobotics.rov.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class ThrusterSensor implements ImmutableValueCompanion<ThrusterSensorValue> {
    public String name;

    public float voltage;

    public float current;

    public float temperature;

    @Override
    public final ThrusterSensorValue asImmutable() {
        return new ThrusterSensorValue(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ThrusterSensor thruster = (ThrusterSensor) o;
        final float epsilon = 0.0001f;
        final Function<Float, Predicate<Float>> withinReason = a ->
            b -> (Float.compare(a, b) == 0 || Math.abs(a - b) < epsilon);
        return (
            withinReason.apply(current).test(thruster.current)
                && withinReason.apply(temperature).test(thruster.temperature)
                && withinReason.apply(voltage).test(thruster.voltage)
                && Objects.equals(name, thruster.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, voltage, current, temperature);
    }
}
