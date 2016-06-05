package com.easternedgerobotics.rov.control;

public final class AnalogToPowerLevel {
    private AnalogToPowerLevel() {
    }

    /**
     * Used to scale the value to the [0, 110] range.
     */
    private static final int SCALAR = 110;

    /**
     * Used to shift the value to the [-5, 105] range.
     */
    private static final int OFFSET = 5;

    /**
     * Lower bound for the result.
     */
    private static final int MINIMUM = 0;

    /**
     * Upper bound for the result.
     */
    private static final int MAXIMUM = 100;

    /**
     * Transform an analog pin value into an appropriate power slider value
     * with dead-bands at the top and bottom of the slider.
     *
     * @param analogValue the input.
     * @return power slider value.
     */
    public static int convert(final float analogValue) {
        return clamp(((1 - analogValue) * SCALAR) - OFFSET);
    }

    /**
     * Clamp the value between the maximum and minimum supported power values.
     *
     * @param value the power slider value.
     * @return clamped value.
     */
    private static int clamp(final float value) {
        if (value < MINIMUM) {
            return MINIMUM;
        }
        if (value > MAXIMUM) {
            return MAXIMUM;
        }
        return Math.round(value);
    }
}
