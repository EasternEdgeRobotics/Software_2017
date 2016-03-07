package com.easternedgerobotics.rov.io;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class DigitalOutputPin implements Pin {

    private final GpioPinDigitalOutput output;

    public DigitalOutputPin(final GpioPinDigitalOutput output) {
        this.output = output;
    }

    @Override
    public final boolean getState() {
        return output.isHigh();
    }

    @Override
    public final void setState(final boolean state) {
        output.setState(state);
    }
}
