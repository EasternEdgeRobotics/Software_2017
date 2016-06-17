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
        joystick.button(CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON).filter(x -> x).map(x -> new VideoFlipValueA())
            .doOnEach(Logger::info)
            .subscribe(eventPublisher::emit);
        joystick.button(CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON).filter(x -> x).map(x -> new VideoFlipValueB())
            .doOnEach(Logger::info)
            .subscribe(eventPublisher::emit);
        joystick.button(MOTION_REVERSE_JOYSTICK_BUTTON).filter(x -> x).subscribe(press -> reverser.toggle());
        joystick.axes().map(motionFunction::apply).map(reverser::apply).subscribe(eventPublisher::emit, Logger::error);

        final Observable<CameraSpeedValueA> cameraForwardA = joystick
            .button(CAMERA_A_MOTOR_FORWARD_JOYSTICK_BUTTON)
            .map(value -> new CameraSpeedValueA(value ? MOTOR_ROTATION_SPEED : 0));
        final Observable<CameraSpeedValueA> cameraReverseA = joystick
            .button(CAMERA_A_MOTOR_REVERSE_JOYSTICK_BUTTON)
            .map(value -> new CameraSpeedValueA(value ? -MOTOR_ROTATION_SPEED : 0));
        cameraForwardA.mergeWith(cameraReverseA)
            .subscribe(eventPublisher::emit, Logger::error);

        final Observable<CameraSpeedValueB> cameraForwardB = joystick
            .button(CAMERA_B_MOTOR_FORWARD_JOYSTICK_BUTTON)
            .map(value -> new CameraSpeedValueB(value ? MOTOR_ROTATION_SPEED : 0));
        final Observable<CameraSpeedValueB> cameraReverseB = joystick
            .button(CAMERA_B_MOTOR_REVERSE_JOYSTICK_BUTTON)
            .map(value -> new CameraSpeedValueB(value ? -MOTOR_ROTATION_SPEED : 0));
        cameraForwardB.mergeWith(cameraReverseB)
            .subscribe(eventPublisher::emit, Logger::error);

        final Observable<ToolingSpeedValue> toolingForward = joystick
            .button(TOOLING_MOTOR_FORWARD_JOYSTICK_BUTTON)
            .map(value -> new ToolingSpeedValue(value ? MOTOR_ROTATION_SPEED : 0));
        final Observable<ToolingSpeedValue> toolingReverse = joystick
            .button(TOOLING_MOTOR_REVERSE_JOYSTICK_BUTTON)
            .map(value -> new ToolingSpeedValue(value ? -MOTOR_ROTATION_SPEED : 0));
        toolingForward.mergeWith(toolingReverse)
            .subscribe(eventPublisher::emit, Logger::error);
    }
}
