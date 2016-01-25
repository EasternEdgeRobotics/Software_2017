package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.JoystickAxesValue;

import rx.Observable;

public interface Joystick {
    public Observable<Boolean> button(final int index);

    public Observable<JoystickAxesValue> axes();
}
