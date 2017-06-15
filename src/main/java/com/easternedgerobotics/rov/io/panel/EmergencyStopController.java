package com.easternedgerobotics.rov.io.panel;

import com.easternedgerobotics.rov.io.devices.IOBoard;
import com.easternedgerobotics.rov.value.DigitalPinValue;

import rx.Observable;

public final class EmergencyStopController {
    /**
     * PilotButton for the emergency stop.
     */
    private final Observable<Boolean> emergencyStop;

    public EmergencyStopController(final IOBoard io, final byte emergencyStopButtonAddress) {
        emergencyStop = io.digitalPin(emergencyStopButtonAddress).map(DigitalPinValue::getValue);
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
