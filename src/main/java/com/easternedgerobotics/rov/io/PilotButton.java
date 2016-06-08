package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.DigitalPinValue;

import rx.Observable;

final class PilotButton {
    /**
     * The arduino instance to communicate with.
     */
    private final Arduino arduino;

    /**
     * The input associated with the button.
     */
    private final byte input;

    /**
     * The output associated with the button.
     */
    private final byte output;

    /**
     * Create a pilot button with no light attached.
     *
     * @param arduino the button interface.
     * @param input the input address.
     */
    PilotButton(final Arduino arduino, final byte input) {
        this(arduino, input, (byte) -1);
    }

    /**
     * Create a pilot button with a light.
     *
     * @param arduino the button interface.
     * @param input the input button address.
     * @param output the output LED address.
     */
    PilotButton(final Arduino arduino, final byte input, final byte output) {
        this.arduino = arduino;
        this.input = input;
        this.output = output;
    }

    /**
     * Observe buttons presses on the pilot panel.
     *
     * @return observable
     */
    Observable<Boolean> click() {
        return arduino.digitalPin(input).map(DigitalPinValue::getValue);
    }

    /**
     * Enable or disable the LED associated with this button.
     *
     * @param value the desired LED state.
     */
    void setLight(final boolean value) {
        if (output >= 0) {
            arduino.setPinValue(output, value);
        }
    }
}
