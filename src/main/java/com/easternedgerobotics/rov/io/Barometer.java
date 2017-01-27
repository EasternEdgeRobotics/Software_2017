package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.InternalPressureValue;

public interface Barometer {

    /**
     * Read pressure device.
     *
     * @return an observable at rate interval
     */
    public InternalPressureValue pressure();
}
