package com.easternedgerobotics.rov.value;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

class Motion implements ImmutableValueCompanion<MotionValue> {
    public float heave;

    public float sway;

    public float surge;

    public float pitch;

    public float yaw;

    public float roll;

    @Override
    public MotionValue asImmutable() {
        return new MotionValue(this);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Motion motion = (Motion) o;
        final float epsilon = 0.0001f;
        final Function<Float, Predicate<Float>> withinReason = a ->
            b -> (Float.compare(a, b) == 0 || Math.abs(a - b) < epsilon);
        return (
               withinReason.apply(heave).test(motion.heave)
            && withinReason.apply(sway).test(motion.sway)
            && withinReason.apply(surge).test(motion.surge)
            && withinReason.apply(pitch).test(motion.pitch)
            && withinReason.apply(yaw).test(motion.yaw)
            && withinReason.apply(roll).test(motion.roll));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(heave, sway, surge, pitch, yaw, roll);
    }
}
