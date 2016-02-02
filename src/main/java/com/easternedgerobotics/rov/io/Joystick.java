package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.JoystickAxesValue;

import rx.Observable;

public interface Joystick {
    Observable<Boolean> button(final int index);

    Observable<JoystickAxesValue> axes();
}
