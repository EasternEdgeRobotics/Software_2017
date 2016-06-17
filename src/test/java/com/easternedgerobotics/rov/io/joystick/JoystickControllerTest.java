package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

public final class JoystickControllerTest {
    @Test
    public final void joystickAxisEventDoesEmitMotionValue() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<MotionValue> subscriber = new TestSubscriber<>();
        final TestSubject<MotionValue> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());
        final Joystick joystick = Mockito.mock(Joystick.class);

        Mockito.when(joystick.axes()).thenReturn(subj);
        Mockito.when(joystick.button(Mockito.anyInt())).thenReturn(Observable.never());
        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(MotionValue.class).subscribe(subscriber);
        subj.onNext(new MotionValue());
        scheduler.triggerActions();

        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new MotionValue()));
    }

    @Test
    public final void forwardCameraRotateButtonDoesEmitCorrectTypeA() {
        final int index = JoystickController.CAMERA_A_MOTOR_FORWARD_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(JoystickController.MOTOR_ROTATION_SPEED), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void forwardCameraRotateButtonDoesEmitCorrectTypeB() {
        final int index = JoystickController.CAMERA_B_MOTOR_FORWARD_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(JoystickController.MOTOR_ROTATION_SPEED), new CameraSpeedValueB(0)
        ));
    }

    @Test
    public final void forwardToolingRotateButtonDoesEmitCorrectType() {
        final int index = JoystickController.TOOLING_MOTOR_FORWARD_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<ToolingSpeedValue> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(ToolingSpeedValue.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new ToolingSpeedValue(JoystickController.MOTOR_ROTATION_SPEED), new ToolingSpeedValue(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesEmitCorrectTypeA() {
        final int index = JoystickController.CAMERA_A_MOTOR_REVERSE_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(-JoystickController.MOTOR_ROTATION_SPEED), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesEmitCorrectTypeB() {
        final int index = JoystickController.CAMERA_B_MOTOR_REVERSE_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(-JoystickController.MOTOR_ROTATION_SPEED), new CameraSpeedValueB(0)
        ));
    }

    @Test
    public final void reverseToolingRotateButtonDoesEmitCorrectType() {
        final int index = JoystickController.TOOLING_MOTOR_REVERSE_JOYSTICK_BUTTON;
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<ToolingSpeedValue> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(ToolingSpeedValue.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new ToolingSpeedValue(-JoystickController.MOTOR_ROTATION_SPEED), new ToolingSpeedValue(0)
        ));
    }
}
