package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class MotionValue implements MutableValueCompanion<Motion> {
    /**
     * Creates a MotionValue with the all the values zeroed out.
     *
     * @return a zero MotionValue
     */
    public static MotionValue zero() {
        return new MotionValue(new Motion());
    }

    /**
     * Creates a MotionValue with the given values.
     *
     * @param heave the linear vertical (up/down) motion
     * @param sway the linear lateral (side-to-side or Port-Starboard) motion
     * @param surge the linear longitudinal (front/back or Bow/Stern) motion
     * @param pitch the Up/down rotation of a vessel about its lateral/Y (side-to-side or Port-Starboasrd) axis
     * @param yaw the turning rotation of a vessel about its vertical/Z axis
     * @param roll the tilting rotation of a vessel about its longitudinal/X (front-back or Bow-Stern) axis
     * @return a MotionValue
     */
    public static MotionValue create(
        final float heave,
        final float sway,
        final float surge,
        final float pitch,
        final float yaw,
        final float roll
    ) {
        final Motion m = new Motion();
        m.heave = heave;
        m.sway = sway;
        m.surge = surge;
        m.pitch = pitch;
        m.yaw = yaw;
        m.roll = roll;
        return new MotionValue(m);
    }

    private final Motion motion;

    MotionValue(final Motion m) {
        this.motion = m;
    }

    public final float getHeave() {
        return motion.heave;
    }

    public final float getSway() {
        return motion.sway;
    }

    public final float getSurge() {
        return motion.surge;
    }

    public final float getPitch() {
        return motion.pitch;
    }

    public final float getYaw() {
        return motion.yaw;
    }

    public final float getRoll() {
        return motion.roll;
    }

    @Override
    public final Motion asMutable() {
        return motion;
    }

    @Override
    public final String toString() {
        return String.format(
            "Motion={%f, %f, %f, %f, %f, %f}",
            motion.heave,
            motion.sway,
            motion.surge,
            motion.pitch,
            motion.yaw,
            motion.roll
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
        final MotionValue that = (MotionValue) o;
        return Objects.equals(motion, that.motion);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(motion);
    }
}
