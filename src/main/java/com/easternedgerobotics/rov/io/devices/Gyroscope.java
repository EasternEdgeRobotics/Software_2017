package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.AngularVelocityValue;

public interface Gyroscope {
    /**
     * Read angular velocity device.
     *
     * @return the current angular velocity
     */
    public AngularVelocityValue angularVelocity();
}
