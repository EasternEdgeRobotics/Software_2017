package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.RotationValue;

public interface Magnetometer {
    /**
     * Read Rotation device.
     *
     * @return the current rotational strengths
     */
    public RotationValue rotation();
}
