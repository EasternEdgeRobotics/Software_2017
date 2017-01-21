package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.RotationValue;

import com.pi4j.io.i2c.I2CBus;
import rx.Observable;

import java.io.IOException;

public final class AltIMU10v3 {
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
     * The rate at which the IMU is polled.
     */
    private final Observable<Long> interval;

    /**
     * Connect to an AltIMUv10 to read the various sensors it contains.
     *
     * @param bus the IMUI2C bus connected to the AltIMU
     * @param saHigh the IMU has 2 address modes. saHigh corresponds to SA high address mode.
     * @param interval the rate at which data is read from the device
     * @throws IOException if the device could not be communicated with
     */
    public AltIMU10v3(
        final I2CBus bus,
        final boolean saHigh,
        final Observable<Long> interval
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
        this.interval = interval;
    }

    /**
     * Read Angular Velocity from the IMU.
     *
     * @return an observable at rate interval
     */
    public Observable<AngularVelocityValue> angularVelocity() {
        return interval.map(l3g::pollAngularVelocity);
    }

    /**
     * Read Pressure from the IMU.
     *
     * @return an observable at rate interval
     */
    public Observable<InternalPressureValue> pressure() {
        return interval.map(lps::pollPressure);
    }

    /**
     * Read Temperature from the IMU.
     *
     * @return an observable at rate interval
     */
    public Observable<InternalTemperatureValue> temperature() {
        return interval.map(lps::pollTemperature);
    }

    /**
     * Read Acceleration from the IMU.
     *
     * @return an observable at rate interval
     */
    public Observable<AccelerationValue> acceleration() {
        return interval.map(lsm::pollAccelerometer);
    }

    /**
     * Read Rotation from the IMU.
     *
     * @return an observable at rate interval
     */
    public Observable<RotationValue> rotation() {
        return interval.map(lsm::pollMagnetometer);
    }
}
