package com.easternedgerobotics.rov.math;

import java.util.function.DoubleFunction;

/**
 * A closed interval with a finite minimum and maximum.
 */
public class Range {
    /**
     * Returns a function to map a given value from Range A to Range B. See also
     * <a href="https://git.io/vwrID">{@code EasternEdge.Common.Utils.ExtensionMethods.NumberRangeMapExtensions}</a>.
     * @param from the range the function will map from
     * @param to the range the resulting function will map into
     * @return a function mapping a given value from one range to another
     */
    public static DoubleFunction<Double> map(final Range from, final Range to) {
        return value -> {
            if (!from.contains(value)) {
                throw new IllegalArgumentException(String.format("The given value must be in %s", from));
            }

            return to.min + (to.max - to.min) * (value - from.min) / (from.max - from.min);
        };
    }

    private final double min;

    private final double max;

    /**
     * Constructs a new Range with the given min and max values.
     * @param min the min value
     * @param max the max value
     */
    public Range(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public final String toString() {
        return String.format("[%f, %f]", min, max);
    }

    private boolean contains(final double value) {
        return min <= value && value <= max;
    }
}
