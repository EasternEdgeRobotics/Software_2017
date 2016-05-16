package com.easternedgerobotics.rov.io.firmata;

public final class AnalogPin {
    private final int identifier;

    private final float value;

    public AnalogPin(final int identifier, final float value) {
        this.identifier = identifier;
        this.value = value;
    }

    public int getIdentifier() {
        return identifier;
    }

    public float getValue() {
        return value;
    }
}
