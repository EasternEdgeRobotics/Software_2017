package com.easternedgerobotics.rov.io.firmata;

public final class DigitalPin {
    private final int identifier;

    private final boolean value;

    public DigitalPin(final int identifier, final boolean value) {
        this.identifier = identifier;
        this.value = value;
    }

    public int getIdentifier() {
        return identifier;
    }

    public boolean isHigh() {
        return value;
    }
}
