package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.InternalTemperatureValue;

public interface Thermometer {
    /**
     * Read temperature device.
     *
     * @return the current temperature
     */
    InternalTemperatureValue temperature();
}
