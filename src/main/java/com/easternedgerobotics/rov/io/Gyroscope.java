package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.AngularVelocityValue;

public interface Gyroscope {
    /**
     * Read angular velocity device.
     *
     * @return the current angular velocity
     */
    public AngularVelocityValue angularVelocity();
}
