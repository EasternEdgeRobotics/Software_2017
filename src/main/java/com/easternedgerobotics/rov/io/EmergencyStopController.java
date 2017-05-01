package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.DigitalPinValue;

import rx.Observable;

public final class EmergencyStopController {
    /**
     * PilotButton for the emergency stop.
     */
    private final Observable<Boolean> emergencyStop;

    public EmergencyStopController(final  Arduino arduino, final byte emergencyStopButtonAddress) {
        emergencyStop = arduino
            .digitalPin(emergencyStopButtonAddress)
            .map(DigitalPinValue::getValue);
    }

    /**
     * Observe changes on the emergency stop buttons.
     *
     * @return observable
     */
    public Observable<Boolean> emergencyStop() {
        return emergencyStop;
    }
}
