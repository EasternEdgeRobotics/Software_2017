package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.AutoDepthValue;
import com.easternedgerobotics.rov.value.DepthValue;
import com.easternedgerobotics.rov.value.MotionValue;

import rx.Observable;

public class AutoDepth {
    private static final float METER_DIFF_MAX = 0.5f;

    private static final float MINIMUM = -0.30f;

    private static final float MAXIMUM = 0.30f;

    private float targetHeight;

    private boolean active = false;

    private float currentHeight;

    public AutoDepth(
        final Observable<AutoDepthValue> autoDepth,
        final Observable<DepthValue> pressure
    ) {
        autoDepth.subscribe(v -> targetHeight = v.getTarget());
        autoDepth.subscribe(v -> active = v.getActive());
        pressure.subscribe(v -> currentHeight = v.getValue());
    }

    public MotionValue apply(
        final MotionValue m
    ) {
        if (!active) {
            return m;
        }
        final float heave = clamp((currentHeight - targetHeight) / METER_DIFF_MAX);
        return new MotionValue(heave, m.getSway(), m.getSurge(), m.getPitch(), m.getYaw(), m.getRoll());
    }

    private static float clamp(final float value) {
        if (value < MINIMUM) {
            return MINIMUM;
        }
        if (value > MAXIMUM) {
            return MAXIMUM;
        }
        return value;
    }
}
