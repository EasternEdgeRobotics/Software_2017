package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.value.MotionValue;

import rx.Observable;

public interface Joystick {
    boolean BUTTON_DOWN = true;

    boolean BUTTON_UP = !BUTTON_DOWN;

    Observable<Boolean> button(final int index);

    Observable<MotionValue> axes();
}
