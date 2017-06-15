package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.PressureValue;

public interface Barometer {

    /**
     * Read pressure device.
     *
     * @return an observable at rate interval
     */
    PressureValue pressure();
}
