package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.JoystickAxesValue;
import com.easternedgerobotics.rov.value.MotionValue;

import rx.Observer;

final class JoystickObserver implements Observer<JoystickAxesValue> {
    private final EventPublisher eventPublisher;

    public JoystickObserver(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onNext(final JoystickAxesValue joystickAxesValue) {
        final float roll = 0;
        final float pitch = 0;
        eventPublisher.emit(MotionValue.create(
            joystickAxesValue.getAxisValue(3),
            joystickAxesValue.getAxisValue(0),
            joystickAxesValue.getAxisValue(1),
            pitch,
            joystickAxesValue.getAxisValue(2),
            roll
        ));
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(final Throwable error) {

    }
}
