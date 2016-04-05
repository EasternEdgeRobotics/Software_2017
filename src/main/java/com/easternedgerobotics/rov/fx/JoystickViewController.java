package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.value.MotionValue;

import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.CompositeSubscription;

import java.util.stream.IntStream;
import javax.inject.Inject;

public class JoystickViewController implements ViewController {
    private final Joystick joystick;

    private final JoystickView view;

    private final CompositeSubscription subscriptions;

    @Inject
    public JoystickViewController(final Joystick joystick, final JoystickView view) {
        this.joystick = joystick;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            joystick.axes()
                .observeOn(JavaFxScheduler.getInstance())
                .subscribe(this::update));
        IntStream.range(1, JoystickView.BUTTON_COUNT + 1).forEach(index ->
            subscriptions.add(
                joystick.button(index)
                    .observeOn(JavaFxScheduler.getInstance())
                    .subscribe(view.buttons.get(index)::setSelected)));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void update(final MotionValue motionValue) {
        view.xAxis.setValue(motionValue.getSway());
        view.yAxis.setValue(motionValue.getSurge());
        view.zAxis.setValue(motionValue.getHeave());
    }
}
