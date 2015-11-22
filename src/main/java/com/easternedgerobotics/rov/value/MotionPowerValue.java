package com.easternedgerobotics.rov.value;

public class MotionPowerValue implements MutableValueCompanion<MotionPower> {
    /**
     * Creates a MotionPowerValue with the all the values zeroed out.
     *
     * @return a zero MotionPowerValue
     */
    public static MotionPowerValue zero() {
        return new MotionPowerValue(new MotionPower());
    }

    /**
     * Creates a MotionPowerValue with the given values.
     *
     * @param global the scalar to all motion
     * @param heave the scalar to linear vertical (up/down) motion
     * @param sway the scalar to linear lateral (side-to-side or Port-Starboard) motion
     * @param surge the scalar to linear longitudinal (front/back or Bow/Stern) motion
     * @param pitch the scalar to Up/down rotation of a vessel about its lateral/Y (side-to-side or Port-Starboard) axis
     * @param yaw the scalar to turning rotation of a vessel about its vertical/Z axis
     * @param roll the scalar to tilting rotation of a vessel about its longitudinal/X (front-back or Bow-Stern) axis
     * @return a MotionPowerValue
     */
    public static MotionPowerValue create(
        final float global,
        final float heave,
        final float sway,
        final float surge,
        final float pitch,
        final float yaw,
        final float roll
    ) {
        final MotionPower mp = new MotionPower();
        mp.global = global;
        mp.heave = heave;
        mp.sway = sway;
        mp.surge = surge;
        mp.pitch = pitch;
        mp.yaw = yaw;
        mp.roll = roll;
        return new MotionPowerValue(mp);
    }

    private final MotionPower motionPower;

    MotionPowerValue(final MotionPower mp) {
        this.motionPower = mp;
    }

    public final float getGlobal() {
        return motionPower.heave;
    }

    public final float getHeave() {
        return motionPower.heave;
    }

    public final float getSway() {
        return motionPower.sway;
    }

    public final float getSurge() {
        return motionPower.surge;
    }

    public final float getPitch() {
        return motionPower.pitch;
    }

    public final float getYaw() {
        return motionPower.yaw;
    }

    public final float getRoll() {
        return motionPower.roll;
    }

    @Override
    public final MotionPower asMutable() {
        return motionPower;
    }

    @Override
    public final String toString() {
        return String.format(
            "MotionPower={%f, %f, %f, %f, %f, %f, %f}",
            motionPower.global,
            motionPower.heave,
            motionPower.sway,
            motionPower.surge,
            motionPower.pitch,
            motionPower.yaw,
            motionPower.roll
        );
    }
}
