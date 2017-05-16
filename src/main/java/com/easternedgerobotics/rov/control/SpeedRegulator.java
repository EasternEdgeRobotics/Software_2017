package com.easternedgerobotics.rov.control;

public final class SpeedRegulator {
    private SpeedRegulator() {

    }

    public static float apply(final boolean forward, final boolean reverse, final boolean toggle, final float speed) {
        if (toggle) {
            return apply(forward, reverse, false, -speed);
        }
        if (forward && !reverse) {
            return speed;
        } else if (!forward && reverse) {
            return -speed;
        }
        return 0;
    }
}
