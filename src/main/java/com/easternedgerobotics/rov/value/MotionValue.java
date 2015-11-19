package com.easternedgerobotics.rov.value;

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
        final Motion motion = new Motion();
        motion.heave = heave;
        motion.sway = sway;
        motion.surge = surge;
        motion.pitch = pitch;
        motion.yaw = yaw;
        motion.roll = roll;
        return new MotionValue(motion);
    }

    private final Motion motion;

    MotionValue(final Motion motion) {
        this.motion = motion;
    }

    public float getHeave() {
        return motion.heave;
    }

    public float getSway() {
        return motion.sway;
    }

    public float getSurge() {
        return motion.surge;
    }

    public float getPitch() {
        return motion.pitch;
    }

    public float getYaw() {
        return motion.yaw;
    }

    public float getRoll() {
        return motion.roll;
    }

    @Override
    public Motion asMutable() {
        return motion;
    }

    @Override
    public String toString() {
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
}
