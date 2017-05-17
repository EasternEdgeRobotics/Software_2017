package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

public final class MotionReverser {
    private MotionReverser() {

    }

    /**
     * If the reverser is enabled, rotate the motion 180 about the yaw/heave axis.
     *
     * @param motion the transformed motion.
     * @return MotionValue
     */
    public static MotionValue apply(final MotionValue motion, final boolean isReversed) {
        if (!isReversed) {
            return motion;
        } else {
            return new MotionValue(
                motion.getHeave(),
                -motion.getSway(),
                -motion.getSurge(),
                -motion.getPitch(),
                motion.getYaw(),
                -motion.getRoll());
        }
    }
}
