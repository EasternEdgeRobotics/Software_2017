package com.easternedgerobotics.rov.control;

public final class AnalogToPowerLevel {
    private AnalogToPowerLevel() {
    }

    /**
     * Used to scale the value to the [0, 110] range.
     */
    private static final float SCALAR = 1.10f;

    /**
     * Used to shift the value to the [-5, 105] range.
     */
    private static final float OFFSET = 0.05f;

    /**
     * Lower bound for the result.
     */
    private static final float MINIMUM = 0;

    /**
     * Upper bound for the result.
     */
    private static final float MAXIMUM = 1.00f;

    /**
     * Transform an analog pin value into an appropriate power slider value
     * with dead-bands at the top and bottom of the slider.
     *
     * @param analogValue the input.
     * @return power slider value.
     */
    public static float convert(final float analogValue) {
        return clamp(((1 - analogValue) * SCALAR) - OFFSET);
    }

    /**
     * Clamp the value between the maximum and minimum supported power values.
     *
     * @param value the power slider value.
     * @return clamped value.
     */
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
