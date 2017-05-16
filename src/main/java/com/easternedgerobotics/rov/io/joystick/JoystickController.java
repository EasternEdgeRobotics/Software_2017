package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoFlipValueB;

import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func4;
import rx.subscriptions.CompositeSubscription;

public final class JoystickController {
    private final CompositeSubscription sourceSubscription = new CompositeSubscription();

    private final CompositeSubscription componentSubscriptions = new CompositeSubscription();

    private final EventPublisher eventPublisher;

    private final Func1<MotionValue, MotionValue> scaleController;

    private final Func2<MotionValue, Boolean, MotionValue> reverseController;

    private final Func4<Boolean, Boolean, Boolean, Float, Float> speedRegulator;

    private final JoystickConfig config;

    public JoystickController(
        final EventPublisher eventPublisher,
        final Func1<MotionValue, MotionValue> scaleController,
        final Func2<MotionValue, Boolean, MotionValue> reverseController,
        final Func4<Boolean, Boolean, Boolean, Float, Float> speedRegulator,
        final JoystickConfig config
    ) {
        this.eventPublisher = eventPublisher;
        this.scaleController = scaleController;
        this.reverseController = reverseController;
        this.speedRegulator = speedRegulator;
        this.config = config;
    }

    public void start(final Observable<Joystick> joystickSource) {
        sourceSubscription.add(joystickSource.subscribe(this::onNext, Logger::error));
    }

    public void stop() {
        sourceSubscription.unsubscribe();
        componentSubscriptions.unsubscribe();
    }

    /**
     * Initializes this controller to read from {@code joystick}.
     */
    private void onNext(final Joystick joystick) {
        componentSubscriptions.clear();

        final Observable<MotionValue> motion = getMotion(joystick);
        final Observable<MotionValue> scaledMotion = getScaledMotion(motion);
        final Observable<MotionValue> pitchedMotion = getPitchedMotion(joystick, scaledMotion);
        final Observable<MotionValue> reversedMotion = getReversedMotion(joystick, pitchedMotion);

        final Observable<VideoFlipValueA> cameraFlipA = getCameraFlipA(joystick);
        final Observable<VideoFlipValueB> cameraFlipB = getCameraFlipB(joystick);
        final Observable<CameraSpeedValueA> cameraSpeedA = getCameraSpeedA(joystick);
        final Observable<CameraSpeedValueB> cameraSpeedB = getCameraSpeedB(joystick);
        final Observable<ToolingSpeedValue> toolingSpeed = getToolingSpeed(joystick);

        componentSubscriptions.add(Observable
            .merge(reversedMotion, cameraFlipA, cameraFlipB, cameraSpeedA, cameraSpeedB, toolingSpeed)
            .subscribe(eventPublisher::emit, Logger::error));
    }

    @SuppressWarnings({"checkstyle:avoidinlineconditionals"})
    private Observable<MotionValue> getMotion(final Joystick joystick) {
        return Observable.combineLatest(
            joystick.axis(config.heaveAxis()).startWith(0f),
            joystick.axis(config.swayAxis()).startWith(0f).map(f -> f != 0 ? -f : 0),
            joystick.axis(config.surgeAxis()).startWith(0f),
            joystick.axis(config.yawAxis()).startWith(0f),
            (heave, sway, surge, yaw) -> new MotionValue(heave, sway, surge, 0, yaw, 0));
    }

    private Observable<MotionValue> getScaledMotion(final Observable<MotionValue> motion) {
        return motion.map(scaleController);
    }

    private Observable<MotionValue> getPitchedMotion(final Joystick joystick, final Observable<MotionValue> motion) {
        final Observable<Float> pitch = Observable.combineLatest(
            joystick.button(config.pitchForwardButton()).startWith(false),
            joystick.button(config.pitchReverseButton()).startWith(false),
            Observable.just(false),
            Observable.just(config.pitchSpeed()),
            speedRegulator);

        return Observable.combineLatest(motion, pitch,
            (m, p) -> new MotionValue(m.getHeave(), m.getSway(), m.getSurge(), p, m.getYaw(), m.getRoll()));
    }

    private Observable<MotionValue> getReversedMotion(final Joystick joystick, final Observable<MotionValue> motion) {
        return Observable.combineLatest(
            motion, joystick.toggleButton(config.motionReverseButton()).startWith(false), reverseController);
    }

    private Observable<VideoFlipValueA> getCameraFlipA(final Joystick joystick) {
        return joystick.toggleButton(config.cameraAVideoFlipButton())
            .map(x -> new VideoFlipValueA());
    }

    private Observable<VideoFlipValueB> getCameraFlipB(final Joystick joystick) {
        return joystick.toggleButton(config.cameraBVideoFlipButton())
            .map(x -> new VideoFlipValueB());
    }

    private Observable<CameraSpeedValueA> getCameraSpeedA(final Joystick joystick) {
        return Observable.combineLatest(
            joystick.button(config.cameraAMotorForwardButton()).startWith(false),
            joystick.button(config.cameraAMotorReverseButton()).startWith(false),
            joystick.toggleButton(config.cameraAVideoFlipButton()).startWith(false),
            Observable.just(config.cameraAMotorSpeed()),
            speedRegulator
        ).map(CameraSpeedValueA::new);
    }

    private Observable<CameraSpeedValueB> getCameraSpeedB(final Joystick joystick) {
        return Observable.combineLatest(
            joystick.button(config.cameraBMotorForwardButton()).startWith(false),
            joystick.button(config.cameraBMotorReverseButton()).startWith(false),
            joystick.toggleButton(config.cameraBVideoFlipButton()).startWith(false),
            Observable.just(config.cameraBMotorSpeed()),
            speedRegulator
        ).map(CameraSpeedValueB::new);
    }

    private Observable<ToolingSpeedValue> getToolingSpeed(final Joystick joystick) {
        return Observable.combineLatest(
            joystick.button(config.toolingMotorForwardButton()).startWith(false),
            joystick.button(config.toolingMotorReverseButton()).startWith(false),
            Observable.just(false),
            Observable.just(config.toolingMotorSpeed()),
            speedRegulator
        ).map(ToolingSpeedValue::new);
    }
}
