package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.devices.Barometer;
import com.easternedgerobotics.rov.io.devices.Thermometer;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.PressureValue;
import com.easternedgerobotics.rov.value.TemperatureValue;

public final class MockPressureSensor implements Barometer, Thermometer {
    private InternalPressureValue pressure = new InternalPressureValue();

    private InternalTemperatureValue temperature = new InternalTemperatureValue();

    public void setPressure(final InternalPressureValue pressure) {
        this.pressure = pressure;
    }

    public void setTemperature(final InternalTemperatureValue temperature) {
        this.temperature = temperature;
    }

    @Override
    public PressureValue pressure() {
        return pressure;
    }

    @Override
    public TemperatureValue temperature() {
        return temperature;
    }
}
