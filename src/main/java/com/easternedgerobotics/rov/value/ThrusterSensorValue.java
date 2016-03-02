package com.easternedgerobotics.rov.value;

import java.util.Objects;

public class ThrusterSensorValue implements MutableValueCompanion<ThrusterSensor> {
    /**
     * Creates a ThrusterSensorValue with the given name.
     *
     * @param name the logical name of the thruster
     * @return a ThrusterSensorValue
     */
    public static ThrusterSensorValue create(
        final String name
    ) {
        return create(name, 0, 0, 0);
    }

    /**
     * Creates a ThrusterSensorValue with the given values.
     *
     * @param name the logical name of the thruster
     * @param voltage the voltage read from the thruster
     * @param current the current read from the thruster
     * @param temperature the temperature read from the thruster
     * @return a ThrusterSensorValue
     */
    public static ThrusterSensorValue create(
        final String name,
        final float voltage,
        final float current,
        final float temperature
    ) {
        final ThrusterSensor t = new ThrusterSensor();
        t.name = name;
        t.voltage = voltage;
        t.current = current;
        t.temperature = temperature;
        return new ThrusterSensorValue(t);
    }

    private final ThrusterSensor thruster;

    ThrusterSensorValue(final ThrusterSensor t) {
        this.thruster = t;
    }

    public final String getName() {
        return thruster.name;
    }

    public final float getVoltage() {
        return thruster.voltage;
    }

    public final float getCurrent() {
        return thruster.current;
    }

    public final float getTemperature() {
        return thruster.temperature;
    }

    @Override
    public final ThrusterSensor asMutable() {
        return thruster;
    }

    @Override
    public final String toString() {
        return String.format(
            "ThrusterSensor={%s, %f, %f, %f}",
            thruster.name,
            thruster.voltage,
            thruster.current,
            thruster.temperature
        );
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ThrusterSensorValue that = (ThrusterSensorValue) o;
        return Objects.equals(thruster, that.thruster);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(thruster);
    }
}
