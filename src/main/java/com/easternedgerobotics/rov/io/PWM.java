package com.easternedgerobotics.rov.io;

/**
 * A Pulse Width Modulation (PWM) output.
 */
public interface PWM {
    /**
     * Sets the maximum forward signal value.
     * @param microseconds the maximum forward signal value
     * @return the same instance
     */
    PWM setMaxForward(float microseconds);

    /**
     * Sets the maximum reverse signal value.
     * @param microseconds the maximum reverse signal value
     * @return the same instance
     */
    PWM setMaxReverse(float microseconds);

    /**
     * Write the given value as a signal.
     * @param value percent from -1 to 1
     */
    void write(float value);

    /**
     * Write zero signal.
     */
    void writeZero();
}
