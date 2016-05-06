package com.easternedgerobotics.rov.value;

public class InternalPressureValue implements MutableValueCompanion<InternalPressure> {
    /**
     * Creates a new {@code InternalTemperatureValue}.
     * @param value the temperature value
     * @return a new {@code InternalTemperatureValue}
     */
    public static InternalPressureValue create(final float value) {
        final InternalPressure temperature = new InternalPressure();
        temperature.pressure = value;
        return new InternalPressureValue(temperature);
    }

    private final InternalPressure internalPressure;

    InternalPressureValue(final InternalPressure internalPressure) {
        this.internalPressure = internalPressure;
    }

    /**
     * Returns the internal pressure value.
     * @return the internal pressure value
     */
    public final float getInternalPressure() {
        return internalPressure.pressure;
    }

    @Override
    public final InternalPressure asMutable() {
        return internalPressure;
    }
}
