package com.easternedgerobotics.rov.measure;

import com.easternedgerobotics.rov.value.DepthValue;
import com.easternedgerobotics.rov.value.ExternalPressureValue;

public final class Depth {
    private static final double KPA_ATMOSPHERIC_PRESSURE = 101.325;

    private static final double GRAVITATIONAL_ACCELERATION = 9.80665;

    private static final double DENSITY_FRESH_WATER = 1000;

    public static DepthValue fromPressure(final ExternalPressureValue pressure) {
        return new DepthValue(fromPressure(pressure.getValue()));
    }

    private static float fromPressure(final float value) {
        return (float) ((value - KPA_ATMOSPHERIC_PRESSURE) / (GRAVITATIONAL_ACCELERATION * DENSITY_FRESH_WATER));
    }

    private Depth() {
        // ???
    }
}
