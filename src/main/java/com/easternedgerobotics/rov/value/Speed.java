package com.easternedgerobotics.rov.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class Speed implements ImmutableValueCompanion<SpeedValue> {
    public String name;

    public float speed;

    @Override
    public final SpeedValue asImmutable() {
        return new SpeedValue(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Speed thruster = (Speed) o;
        final float epsilon = 0.0001f;
        final Function<Float, Predicate<Float>> withinReason = a ->
            b -> (Float.compare(a, b) == 0 || Math.abs(a - b) < epsilon);
        return (withinReason.apply(speed).test(thruster.speed) && Objects.equals(name, thruster.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, speed);
    }
}
