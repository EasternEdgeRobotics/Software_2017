package com.easternedgerobotics.rov.io;

public interface Pin {

    /**
     * Get the state of the GPIO pin.
     *
     * @return true if the pin is high.
     */
    boolean getState();

    /**
     * Set the state of the GPIO pin.
     *
     * @param state true if the pin should be high.
     */
    void setState(final boolean state);
}
