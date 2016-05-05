package com.easternedgerobotics.rov.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class InternalTemperature implements ImmutableValueCompanion<InternalTemperatureValue> {
    public float temperature;

    @Override
    public final InternalTemperatureValue asImmutable() {
        return new InternalTemperatureValue(this);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final InternalTemperature that = (InternalTemperature) o;
        final float epsilon = 0.0001f;
        final Function<Float, Predicate<Float>> withinReason = a ->
            b -> (Float.compare(a, b) == 0 || Math.abs(a - b) < epsilon);
        return (withinReason.apply(temperature).test(that.temperature));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(temperature);
    }
}
