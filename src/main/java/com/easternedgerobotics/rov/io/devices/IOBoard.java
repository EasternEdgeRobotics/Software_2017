package com.easternedgerobotics.rov.io.devices;

import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.DigitalPinValue;

import rx.Observable;

public interface IOBoard {
    /**
     * Observe change events from a specific digital pin.
     *
     * @param address the physical location of the pin.
     * @return Observable
     */
    Observable<DigitalPinValue> digitalPin(final byte address);

    /**
     * Observe change events from a specific analog pin.
     *
     * @param address the physical location of the pin.
     * @return Observable
     */
    Observable<AnalogPinValue> analogPin(final byte address);

    /**
     * Set the value of a digital pin.
     *
     * @param address the physical location of the pin.
     * @param value the desired state of the pin.
     */
    void setPinValue(final byte address, final boolean value);
}
