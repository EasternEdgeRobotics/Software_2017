package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.math.Range;

/**
 * A Pulse Width Modulation (PWM) output.
 */
public interface PWM {
    /**
     * Write the given value as a signal.
     * @param value percent from -1 to 1
     */
    void write(float value);

    /**
     * Write zero signal.
     */
    void writeZero();

    /**
     * Sets the output signal range.
     * @param range the output signal range
     * @return the PWM instance
     */
    PWM setOutputRange(Range range);
}
