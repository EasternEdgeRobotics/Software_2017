package com.easternedgerobotics.rov.value;

public class InternalTemperatureValue implements MutableValueCompanion<InternalTemperature> {
    /**
     * Creates a new {@code InternalTemperatureValue}.
     * @param value the temperature value
     * @return a new {@code InternalTemperatureValue}
     */
    public static InternalTemperatureValue create(final float value) {
        final InternalTemperature temperature = new InternalTemperature();
        temperature.temperature = value;
        return new InternalTemperatureValue(temperature);
    }

    private final InternalTemperature internalTemperature;

    InternalTemperatureValue(final InternalTemperature internalTemperature) {
        this.internalTemperature = internalTemperature;
    }

    /**
     * Returns the internal temperature value.
     * @return the internal temperature value
     */
    public final float getInternalTemperature() {
        return internalTemperature.temperature;
    }

    @Override
    public final InternalTemperature asMutable() {
        return internalTemperature;
    }
}
