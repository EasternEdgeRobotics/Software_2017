package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.AccelerationValue;

public interface Accelerometer {

    /**
     * Read acceleration device.
     *
     * @return the current acceleration
     */
    AccelerationValue acceleration();
}

