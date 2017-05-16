package com.easternedgerobotics.rov.io.joystick;

import rx.Observable;

public interface Joystick {
    boolean BUTTON_DOWN = true;

    boolean BUTTON_UP = !BUTTON_DOWN;

    Observable<Boolean> button(final String name);

    Observable<Boolean> toggleButton(final String name);

    Observable<Float> axis(final String name);
}
