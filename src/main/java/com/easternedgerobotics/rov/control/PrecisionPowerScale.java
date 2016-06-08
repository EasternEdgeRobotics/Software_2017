package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PrecisionPowerValue;

public final class PrecisionPowerScale {
    /**
     * State of the control.
     */
    private volatile boolean enabled;

    /**
     * Set the state of the control.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * If the reverser is enabled, rotate the motion 180 about the yaw/heave axis.
     *
     * @param motion the transformed motion.
     * @return MotionValue
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    public MotionValue apply(final MotionValue motion, final PrecisionPowerValue precision) {
        if (!enabled) {
            return motion;
        } else {
            final float power = precision.getPower();
            return new MotionValue(
                precision.getHeave() ? motion.getHeave() * power : motion.getHeave(),
                precision.getSway() ? motion.getSway() * power : motion.getSway(),
                precision.getSurge() ? motion.getSurge() * power : motion.getSurge(),
                precision.getPitch() ? motion.getPitch() * power : motion.getPitch(),
                precision.getYaw() ? motion.getYaw() * power : motion.getYaw(),
                precision.getRoll() ? motion.getRoll() * power : motion.getRoll()
            );
        }
    }
}
