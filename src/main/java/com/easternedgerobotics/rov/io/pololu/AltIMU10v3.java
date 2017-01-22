package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.RotationValue;

import com.pi4j.io.i2c.I2CBus;

import java.io.IOException;

public final class AltIMU10v3
    implements Accelerometer, Barometer, Thermometer, Gyroscope, Magnetometer {
    /**
     * Digital barometer instance.
     */
    private final LPS331AP lps;

    /**
     * Digital gyro instance.
     */
    private final L3GD20H l3g;

    /**
     * Digital accelerometer and magnetometer instance.
     */
    private final LSM303D lsm;

    /**
     * Connect to an AltIMUv10 to read the various sensors it contains.
     *
     * @param bus the IMUI2C bus connected to the AltIMU
     * @param saHigh the IMU has 2 address modes. saHigh corresponds to SA high address mode.
     * @throws IOException if the device could not be communicated with
     */
    public AltIMU10v3(
        final I2CBus bus,
        final boolean saHigh
    ) throws IOException {
        if (saHigh) {
            lps = new LPS331AP(new IMUI2C(bus.getDevice(LPS331AP.SA0_HIGH_ADDRESS)));
            l3g = new L3GD20H(new IMUI2C(bus.getDevice(L3GD20H.SA0_HIGH_ADDRESS)));
            lsm = new LSM303D(new IMUI2C(bus.getDevice(LSM303D.SA0_HIGH_ADDRESS)));
        } else {
            lps = new LPS331AP(new IMUI2C(bus.getDevice(LPS331AP.SA0_LOW_ADDRESS)));
            l3g = new L3GD20H(new IMUI2C(bus.getDevice(L3GD20H.SA0_LOW_ADDRESS)));
            lsm = new LSM303D(new IMUI2C(bus.getDevice(LSM303D.SA0_LOW_ADDRESS)));
        }
    }

    @Override
    public AngularVelocityValue angularVelocity() {
        return l3g.pollAngularVelocity();
    }

    @Override
    public InternalPressureValue pressure() {
        return lps.pollPressure();
    }

    @Override
    public InternalTemperatureValue temperature() {
        return lps.pollTemperature();
    }

    @Override
    public AccelerationValue acceleration() {
        return lsm.pollAccelerometer();
    }

    @Override
    public RotationValue rotation() {
        return lsm.pollMagnetometer();
    }
}
