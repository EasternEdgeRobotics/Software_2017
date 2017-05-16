package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

public final class ExponentialMotionScale {
    private ExponentialMotionScale() {

    }

    /**
     * Applies exponential scaling to the given motion value.
     *
     * @param motionValue the input motion value
     * @return the scaled motion value output
     */
    public static MotionValue apply(final MotionValue motionValue) {
        return new MotionValue(
            scale(motionValue.getHeave()),
            scale(motionValue.getSway()),
            scale(motionValue.getSurge()),
            scale(motionValue.getPitch()),
            scale(motionValue.getYaw()),
            scale(motionValue.getRoll())
        );
    }

    /**
     * Apply exponential scaling to the input.
     * <p>
     * See <a href="https://github.com/EasternEdgeRobotics/2016/issues/107">#107</a> for background.
     * @param x the function input
     * @return the scaled value
     */
    private static float scale(final float x) {
        if (x < 0) {
            return -scale(-x);
        }

        return (float) ((Math.exp(x) - 1) / (Math.E - 1));
    }
}
