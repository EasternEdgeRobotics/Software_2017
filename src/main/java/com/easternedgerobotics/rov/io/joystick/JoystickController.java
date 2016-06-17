package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.control.MotionReverser;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoFlipValueB;

import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Observer;

import java.util.function.Function;

public final class JoystickController implements Observer<Joystick> {
    static final int CAMERA_A_MOTOR_FORWARD_JOYSTICK_BUTTON = 4;

    static final int CAMERA_A_MOTOR_REVERSE_JOYSTICK_BUTTON = 6;

    static final int CAMERA_B_MOTOR_FORWARD_JOYSTICK_BUTTON = 5;

    static final int CAMERA_B_MOTOR_REVERSE_JOYSTICK_BUTTON = 3;

    static final int TOOLING_MOTOR_FORWARD_JOYSTICK_BUTTON = 11;

    static final int TOOLING_MOTOR_REVERSE_JOYSTICK_BUTTON = 12;

    static final int MOTION_REVERSE_JOYSTICK_BUTTON = 2;

    static final int CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON = 7;

    static final int CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON = 8;

    static final float MOTOR_ROTATION_SPEED = 0.3f;

    private final EventPublisher eventPublisher;

    private final Function<MotionValue, MotionValue> motionFunction;

    public JoystickController(
        final EventPublisher eventPublisher,
        final Function<MotionValue, MotionValue> motionFunction
    ) {
        this.eventPublisher = eventPublisher;
        this.motionFunction = motionFunction;
    }

    @Override
    public void onCompleted() {
        // ???
    }

    @Override
    public void onError(final Throwable e) {
        Logger.warn(e);
    }

    @Override
    public void onNext(final Joystick joystick) {
        final MotionReverser reverser = new MotionReverser();

        joystick.axes().map(motionFunction::apply).map(reverser::apply).subscribe(eventPublisher::emit, Logger::error);

        joystick.button(MOTION_REVERSE_JOYSTICK_BUTTON)
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .subscribe(press -> reverser.toggle());

        initCameraFlipA(joystick);
        initCameraFlipB(joystick);

        initCameraMotorA(joystick);
        initCameraMotorB(joystick);
        initToolingMotor(joystick);
    }

    private void initCameraFlipA(final Joystick joystick) {
        joystick.button(CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON)
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .map(x -> new VideoFlipValueA())
            .subscribe(eventPublisher::emit);
    }

    private void initCameraFlipB(final Joystick joystick) {
        joystick.button(CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON)
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .map(x -> new VideoFlipValueB())
            .subscribe(eventPublisher::emit);
    }

    private void initCameraMotorA(final Joystick joystick) {
        final Observable<CameraSpeedValueA> fwd = joystick.button(CAMERA_A_MOTOR_FORWARD_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new CameraSpeedValueA(MOTOR_ROTATION_SPEED);
            }

            return new CameraSpeedValueA(0);
        });
        final Observable<CameraSpeedValueA> rev = joystick.button(CAMERA_A_MOTOR_REVERSE_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new CameraSpeedValueA(-MOTOR_ROTATION_SPEED);
            }

            return new CameraSpeedValueA(0);
        });

        Observable.merge(fwd, rev).subscribe(eventPublisher::emit, Logger::error);
    }

    private void initCameraMotorB(final Joystick joystick) {
        final Observable<CameraSpeedValueB> fwd = joystick.button(CAMERA_B_MOTOR_FORWARD_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new CameraSpeedValueB(MOTOR_ROTATION_SPEED);
            }

            return new CameraSpeedValueB(0);
        });
        final Observable<CameraSpeedValueB> rev = joystick.button(CAMERA_B_MOTOR_REVERSE_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new CameraSpeedValueB(-MOTOR_ROTATION_SPEED);
            }

            return new CameraSpeedValueB(0);
        });

        Observable.merge(fwd, rev).subscribe(eventPublisher::emit, Logger::error);
    }

    private void initToolingMotor(final Joystick joystick) {
        final Observable<ToolingSpeedValue> fwd = joystick.button(TOOLING_MOTOR_FORWARD_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new ToolingSpeedValue(MOTOR_ROTATION_SPEED);
            }

            return new ToolingSpeedValue(0);
        });
        final Observable<ToolingSpeedValue> rev = joystick.button(TOOLING_MOTOR_REVERSE_JOYSTICK_BUTTON).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new ToolingSpeedValue(-MOTOR_ROTATION_SPEED);
            }

            return new ToolingSpeedValue(0);
        });

        Observable.merge(fwd, rev).subscribe(eventPublisher::emit, Logger::error);
    }
}
