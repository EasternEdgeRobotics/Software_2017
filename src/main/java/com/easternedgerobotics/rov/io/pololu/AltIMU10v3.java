package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.I2C;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.RotationValue;

import java.util.List;

public final class AltIMU10v3 implements Accelerometer, Barometer, Thermometer, Gyroscope, Magnetometer {
    /**
     * The PololuI2C addresses SA0 is connected to power (high).
     */
    static final byte L3GD20H_SA0_HIGH_ADDRESS = 0b01101011;

    /**
     * The PololuI2C addresses SA0 is grounded (low).
     */
    static final byte L3GD20H_SA0_LOW_ADDRESS = 0b01101010;

    /**
     * The PololuI2C addresses SA0 is connected to power (high).
     */
    static final byte LPS331AP_SA0_LOW_ADDRESS = 0b1011100;

    /**
     * The PololuI2C addresses SA0 is grounded (low).
     */
    static final byte LPS331AP_SA0_HIGH_ADDRESS = 0b1011101;

    /**
     * The PololuI2C addresses SA0 is connected to power (high).
     */
    static final byte LSM303D_SA0_HIGH_ADDRESS = 0b0011101;

    /**
     * The PololuI2C addresses SA0 is grounded (low).
     */
    static final byte LSM303D_SA0_LOW_ADDRESS = 0b0011110;

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
     * @param i2cDevices the AltIMU10v3 I2C connections
     */
    public AltIMU10v3(final List<I2C> i2cDevices, final boolean sa0High) {
        if (sa0High) {
            lps = new LPS331AP(i2cDevices.get(LPS331AP_SA0_HIGH_ADDRESS));
            l3g = new L3GD20H(i2cDevices.get(L3GD20H_SA0_HIGH_ADDRESS));
            lsm = new LSM303D(i2cDevices.get(LSM303D_SA0_HIGH_ADDRESS));
        } else {
            lps = new LPS331AP(i2cDevices.get(LPS331AP_SA0_LOW_ADDRESS));
            l3g = new L3GD20H(i2cDevices.get(L3GD20H_SA0_LOW_ADDRESS));
            lsm = new LSM303D(i2cDevices.get(LSM303D_SA0_LOW_ADDRESS));
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
