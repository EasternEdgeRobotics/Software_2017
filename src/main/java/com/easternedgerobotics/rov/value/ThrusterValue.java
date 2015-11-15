package com.easternedgerobotics.rov.value;

public class ThrusterValue implements MutableValueCompanion<Thruster> {
    /**
     * Creates a ThrusterValue with the all the values zeroed out.
     *
     * @return a zero ThrusterValue
     */
    public static ThrusterValue zero() {
        return new ThrusterValue(new Thruster());
    }

    /**
     * Creates a ThrusterValue with the given values.
     *
     * @param name the logical name of the thruster
     * @param speed the desired speed to set the thruster
     * @param voltage the voltage read from the thruster
     * @param current the current read from the thruster
     * @param temperature the temperature read from the thruster
     * @return a ThrusterValue
     */
    public static ThrusterValue create(
        final String name,
        final float speed,
        final float voltage,
        final float current,
        final float temperature
    ) {
        final Thruster thruster = new Thruster();
        thruster.name = name;
        thruster.speed = speed;
        thruster.voltage = voltage;
        thruster.current = current;
        thruster.temperature = temperature;
        return new ThrusterValue(thruster);
    }

    public static ThrusterValue setSpeed(
        final ThrusterValue old,
        final float speed
    ) {
        final Thruster thruster = new Thruster();
        thruster.name = old.getName();
        thruster.speed = speed;
        thruster.voltage = old.getVoltage();
        thruster.current = old.getCurrent();
        thruster.temperature = old.getTemperature();
        return new ThrusterValue(thruster);
    }

    private final Thruster thruster;

    ThrusterValue(final Thruster thruster) {
        this.thruster = thruster;
    }

    public String getName() {
        return thruster.name;
    }

    public float getSpeed() {
        return thruster.speed;
    }

    public float getVoltage() {
        return thruster.voltage;
    }

    public float getCurrent() {
        return thruster.current;
    }

    public float getTemperature() {
        return thruster.temperature;
    }

    @Override
    public Thruster asMutable() {
        return thruster;
    }

    @Override
    public String toString() {
        return String.format(
            "Thruster={%s, %f, %f, %f, %f}",
            thruster.name,
            thruster.speed,
            thruster.voltage,
            thruster.current,
            thruster.temperature
        );
    }
}
