package com.easternedgerobotics.rov.io;

import com.pi4j.io.gpio.GpioPinDigitalInput;


public class DigitalInputPin implements Pin {

    private final GpioPinDigitalInput input;

    public DigitalInputPin(final GpioPinDigitalInput input) {
        this.input = input;
    }

    @Override
    public final boolean getState() {
        return input.isHigh();
    }

    @Override
    public final void setState(final boolean state) {
        throw new IllegalStateException("DigitalInputPin %s cannot be set");
    }
}
