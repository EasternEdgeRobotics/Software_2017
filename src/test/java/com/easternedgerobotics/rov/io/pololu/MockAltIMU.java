package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.Accelerometer;
import com.easternedgerobotics.rov.io.devices.Barometer;
import com.easternedgerobotics.rov.io.devices.Gyroscope;
import com.easternedgerobotics.rov.io.devices.Magnetometer;
import com.easternedgerobotics.rov.io.devices.Thermometer;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.PressureValue;
import com.easternedgerobotics.rov.value.RotationValue;
import com.easternedgerobotics.rov.value.TemperatureValue;

public final class MockAltIMU implements Accelerometer, Barometer, Thermometer, Gyroscope, Magnetometer {
    private AccelerationValue acceleration = new AccelerationValue();

    private InternalPressureValue pressure = new InternalPressureValue();

    private AngularVelocityValue angularVelocity = new AngularVelocityValue();

    private RotationValue rotation = new RotationValue();

    private InternalTemperatureValue temperature = new InternalTemperatureValue();

    public void setAcceleration(final AccelerationValue acceleration) {
        this.acceleration = acceleration;
    }

    public void setPressure(final InternalPressureValue pressure) {
        this.pressure = pressure;
    }

    public void setAngularVelocity(final AngularVelocityValue angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public void setRotation(final RotationValue rotation) {
        this.rotation = rotation;
    }

    public void setTemperature(final InternalTemperatureValue temperature) {
        this.temperature = temperature;
    }

    @Override
    public AccelerationValue acceleration() {
        return acceleration;
    }

    @Override
    public PressureValue pressure() {
        return pressure;
    }

    @Override
    public AngularVelocityValue angularVelocity() {
        return angularVelocity;
    }

    @Override
    public RotationValue rotation() {
        return rotation;
    }

    @Override
    public TemperatureValue temperature() {
        return temperature;
    }
}
