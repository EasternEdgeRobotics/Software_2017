package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

public final class MotionReverser {
    /**
     * State of the reverser.
     */
    private volatile boolean isReversed;

    /**
     * Toggle the state of the reverser.
     */
    public void toggle() {
        isReversed = !isReversed;
    }

    /**
     * If the reverser is enabled, rotate the motion 180 about the yaw/heave axis.
     *
     * @param motion the transformed motion.
     * @return MotionValue
     */
    public MotionValue apply(final MotionValue motion) {
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
