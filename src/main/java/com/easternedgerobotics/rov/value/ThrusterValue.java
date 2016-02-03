package com.easternedgerobotics.rov.value;

public class ThrusterValue implements MutableValueCompanion<Thruster> {
    /**
     * Creates a ThrusterValue with the given name.
     *
     * @param name the logical name of the thruster
     * @return a ThrusterValue
     */
    public static ThrusterValue create(
        final String name
    ) {
        return create(name, 0, 0, 0, 0);
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
        final Thruster t = new Thruster();
        t.name = name;
        t.speed = speed;
        t.voltage = voltage;
        t.current = current;
        t.temperature = temperature;
        return new ThrusterValue(t);
    }

    private final Thruster thruster;

    ThrusterValue(final Thruster t) {
        this.thruster = t;
    }

    public final ThrusterValue setSpeed(
        final float speed
    ) {
        return ThrusterValue.create(
            thruster.name,
            speed,
            thruster.voltage,
            thruster.current,
            thruster.temperature
        );
    }

    public final String getName() {
        return thruster.name;
    }

    public final float getSpeed() {
        return thruster.speed;
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
    public final Thruster asMutable() {
        return thruster;
    }

    @Override
    public final String toString() {
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
