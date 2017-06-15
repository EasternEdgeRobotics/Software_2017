package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.TemperatureValue;

public interface Thermometer {
    /**
     * Read temperature device.
     *
     * @return the current temperature
     */
    TemperatureValue temperature();
}
