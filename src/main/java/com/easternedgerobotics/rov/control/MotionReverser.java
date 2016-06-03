package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

public final class MotionReverser {
    private boolean isReversed;

    public void toggle() {
        isReversed = !isReversed;
    }

    public MotionValue apply(final MotionValue motion) {
        if (!isReversed) {
            return motion;
        } else {
            return new MotionValue(
                motion.getHeave(),
                -motion.getSway(),
                -motion.getSurge(),
                -motion.getPitch(),
                -motion.getRoll(),
                motion.getYaw()
            );
        }
    }
}
