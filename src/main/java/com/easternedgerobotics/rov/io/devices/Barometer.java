package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.InternalPressureValue;

public interface Barometer {

    /**
     * Read pressure device.
     *
     * @return an observable at rate interval
     */
    public InternalPressureValue pressure();
}
