package com.easternedgerobotics.rov.io.rpi;

import com.easternedgerobotics.rov.io.devices.Light;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public final class RPiGPIOLight implements Light {
    /**
     * The raspi gpio pin being used.
     */
    private final GpioPinDigitalOutput pin;

    /**
     * Flashing period for the lights.
     */
    private static final int PERIOD = 600;

    public RPiGPIOLight(final int pinNumber) {
        final GpioController gpioController = GpioFactory.getInstance();
        pin = gpioController.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + pinNumber));
    }

    @Override
    public void write(final boolean value) {
        pin.setState(value);
    }

    @Override
    public void flash() {
        pin.blink(PERIOD);
    }

}
