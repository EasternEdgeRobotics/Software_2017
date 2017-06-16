package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.config.MockJoystickConfig;
import com.easternedgerobotics.rov.control.MotionReverser;
import com.easternedgerobotics.rov.control.SpeedRegulator;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ToolingASpeedValue;

import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class JoystickControllerTest {
    private final JoystickConfig config;

    public JoystickControllerTest() {
        config = new MockJoystickConfig();
    }

    private JoystickController getJoystickController(final EventPublisher eventPublisher) {
        return new JoystickController(
            eventPublisher,
            m -> m,
            MotionReverser::apply,
            SpeedRegulator::apply,
            m -> m,
            config);
    }

    @Test
    public final void joystickAxisEventDoesEmitMotionValue() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<MotionValue> subscriber = new TestSubscriber<>();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final TestSubject<Float> subj = TestSubject.create(scheduler);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(AdditionalMatchers.not(ArgumentMatchers.matches(config.heaveAxis()))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.axis(Mockito.eq(config.heaveAxis()))).thenReturn(subj);
        Mockito.when(joystick.button(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(MotionValue.class).subscribe(subscriber);
        subj.onNext(1f);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new MotionValue(), new MotionValue(1f, 0, 0, 0, 0, 0)));
    }

    @Test
    public final void forwardCameraRotateButtonDoesEmitCorrectTypeA() {
        final String name = config.cameraAMotorForwardButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(0), new CameraSpeedValueA(config.cameraAMotorSpeed()), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void forwardCameraRotateButtonDoesEmitCorrectTypeB() {
        final String name = config.cameraBMotorForwardButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(0), new CameraSpeedValueB(config.cameraBMotorSpeed()), new CameraSpeedValueB(0)
        ));
    }

    @Test
    public final void forwardToolingRotateButtonDoesEmitCorrectType() {
        final String name = config.toolingAMotorForwardButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<ToolingASpeedValue> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(ToolingASpeedValue.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new ToolingASpeedValue(0), new ToolingASpeedValue(config.toolingAMotorSpeed()), new ToolingASpeedValue(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesEmitCorrectTypeA() {
        final String name = config.cameraAMotorReverseButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(0), new CameraSpeedValueA(-config.cameraAMotorSpeed()), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesEmitCorrectTypeB() {
        final String name = config.cameraBMotorReverseButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(0), new CameraSpeedValueB(-config.cameraBMotorSpeed()), new CameraSpeedValueB(0)
        ));
    }

    @Test
    public final void reverseToolingRotateButtonDoesEmitCorrectType() {
        final String name = config.toolingAMotorReverseButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<ToolingASpeedValue> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name))).thenReturn(subj);
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(ToolingASpeedValue.class).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(3);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new ToolingASpeedValue(0), new ToolingASpeedValue(-config.toolingAMotorSpeed()), new ToolingASpeedValue(0)
        ));
    }

    @Test
    public final void forwardCameraRotateButtonDoesFlipWhenCameraIsFlippedA() {
        final String name = config.cameraAMotorForwardButton();
        final String flipName = config.cameraAVideoFlipButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> motorForwardButton = TestSubject.create(scheduler);
        final TestSubject<Boolean> videoFlipCamera = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(ArgumentMatchers.matches(name))).thenReturn(motorForwardButton);
        Mockito.when(joystick.toggleButton(AdditionalMatchers.not(ArgumentMatchers.matches(flipName))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.matches(flipName))).thenReturn(videoFlipCamera);

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        motorForwardButton.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_UP);
        videoFlipCamera.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(0), new CameraSpeedValueA(config.cameraAMotorSpeed()), new CameraSpeedValueA(0),
            new CameraSpeedValueA(0), new CameraSpeedValueA(-config.cameraAMotorSpeed()), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void forwardCameraRotateButtonDoesFlipWhenCameraIsFlippedB() {
        final String name = config.cameraBMotorForwardButton();
        final String flipName = config.cameraBVideoFlipButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> motorForwardButton = TestSubject.create(scheduler);
        final TestSubject<Boolean> videoFlipCamera = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(ArgumentMatchers.matches(name))).thenReturn(motorForwardButton);
        Mockito.when(joystick.toggleButton(AdditionalMatchers.not(ArgumentMatchers.matches(flipName))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.matches(flipName))).thenReturn(videoFlipCamera);

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        motorForwardButton.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_UP);
        videoFlipCamera.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_DOWN);
        motorForwardButton.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(0), new CameraSpeedValueB(config.cameraBMotorSpeed()), new CameraSpeedValueB(0),
            new CameraSpeedValueB(0), new CameraSpeedValueB(-config.cameraBMotorSpeed()), new CameraSpeedValueB(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesFlipWhenCameraIsFlippedA() {
        final String name = config.cameraAMotorReverseButton();
        final String flipName = config.cameraAVideoFlipButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueA> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> motorReverseButton = TestSubject.create(scheduler);
        final TestSubject<Boolean> videoFlipCamera = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(ArgumentMatchers.matches(name))).thenReturn(motorReverseButton);
        Mockito.when(joystick.toggleButton(AdditionalMatchers.not(ArgumentMatchers.matches(flipName))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.matches(flipName))).thenReturn(videoFlipCamera);

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueA.class).subscribe(subscriber);
        motorReverseButton.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_UP);
        videoFlipCamera.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueA(0), new CameraSpeedValueA(-config.cameraAMotorSpeed()), new CameraSpeedValueA(0),
            new CameraSpeedValueA(0), new CameraSpeedValueA(config.cameraAMotorSpeed()), new CameraSpeedValueA(0)
        ));
    }

    @Test
    public final void reverseCameraRotateButtonDoesFlipWhenCameraIsFlippedB() {
        final String name = config.cameraBMotorReverseButton();
        final String flipName = config.cameraBVideoFlipButton();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<CameraSpeedValueB> subscriber = new TestSubscriber<>();
        final TestSubject<Boolean> motorReverseButton = TestSubject.create(scheduler);
        final TestSubject<Boolean> videoFlipCamera = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(ArgumentMatchers.matches(name))).thenReturn(motorReverseButton);
        Mockito.when(joystick.toggleButton(AdditionalMatchers.not(ArgumentMatchers.matches(flipName))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.matches(flipName))).thenReturn(videoFlipCamera);

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(CameraSpeedValueB.class).subscribe(subscriber);
        motorReverseButton.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_UP);
        videoFlipCamera.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_DOWN);
        motorReverseButton.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertReceivedOnNext(Arrays.asList(
            new CameraSpeedValueB(0), new CameraSpeedValueB(-config.cameraBMotorSpeed()), new CameraSpeedValueB(0),
            new CameraSpeedValueB(0), new CameraSpeedValueB(config.cameraBMotorSpeed()), new CameraSpeedValueB(0)
        ));
    }
}
