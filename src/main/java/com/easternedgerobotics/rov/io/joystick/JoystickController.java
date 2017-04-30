package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.config.JoystickConfig;
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class JoystickController {
    private final EventPublisher eventPublisher;

    private final Function<MotionValue, MotionValue> motionFunction;

    private final JoystickConfig config;

    public JoystickController(
        final EventPublisher eventPublisher,
        final Function<MotionValue, MotionValue> motionFunction,
        final JoystickConfig config
    ) {
        this.eventPublisher = eventPublisher;
        this.motionFunction = motionFunction;
        this.config = config;
    }

    /**
     * Initializes this controller to read from {@code joystick}.
     */
    public void onNext(final Joystick joystick) {
        final MotionReverser reverser = new MotionReverser();

        joystick.axes().map(motionFunction::apply).map(reverser::apply).subscribe(eventPublisher::emit, Logger::error);

        joystick.button(config.motionReverseButton())
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .subscribe(press -> reverser.toggle());

        initCameraFlipA(joystick);
        initCameraFlipB(joystick);

        initCameraMotorA(joystick);
        initCameraMotorB(joystick);
        initToolingMotor(joystick);
    }

    private void initCameraFlipA(final Joystick joystick) {
        joystick.button(config.cameraAVideoFlipButton())
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .map(x -> new VideoFlipValueA())
            .subscribe(eventPublisher::emit);
    }

    private void initCameraFlipB(final Joystick joystick) {
        joystick.button(config.cameraBVideoFlipButton())
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .map(x -> new VideoFlipValueB())
            .subscribe(eventPublisher::emit);
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    private void initCameraMotorA(final Joystick joystick) {
        // This needs to be a reference to a boolean, it being atomic is irrelevant
        final AtomicBoolean flipped = new AtomicBoolean(true);
        final Observable<Boolean> fwd = joystick.button(config.cameraAMotorForwardButton());
        final Observable<Boolean> rev = joystick.button(config.cameraAMotorReverseButton());
        final Observable<Boolean> flips = joystick.button(config.cameraAVideoFlipButton())
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .startWith(Joystick.BUTTON_DOWN);

        Observable.switchOnNext(flips.map(flip -> {
            flipped.set(!flipped.get());
            return Observable.merge(
                fwd.map(x -> new CameraSpeedValueA(
                    x ? (flipped.get() ? -config.motorRotationSpeed() : config.motorRotationSpeed()) : (float) 0
                )),
                rev.map(x -> new CameraSpeedValueA(
                    x ? (flipped.get() ? config.motorRotationSpeed() : -config.motorRotationSpeed()) : (float) 0
                ))
            );
        })).subscribe(eventPublisher::emit, Logger::error);
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    private void initCameraMotorB(final Joystick joystick) {
        // This needs to be a reference to a boolean, it being atomic is irrelevant
        final AtomicBoolean flipped = new AtomicBoolean(true);
        final Observable<Boolean> fwd = joystick.button(config.cameraBMotorForwardButton());
        final Observable<Boolean> rev = joystick.button(config.cameraBMotorReverseButton());
        final Observable<Boolean> flips = joystick.button(config.cameraBVideoFlipButton())
            .filter(x -> x == Joystick.BUTTON_DOWN)
            .startWith(Joystick.BUTTON_DOWN);

        Observable.switchOnNext(flips.map(flip -> {
            flipped.set(!flipped.get());
            return Observable.merge(
                fwd.map(x -> new CameraSpeedValueB(
                    x ? (flipped.get() ? -config.motorRotationSpeed() : config.motorRotationSpeed()) : (float) 0
                )),
                rev.map(x -> new CameraSpeedValueB(
                    x ? (flipped.get() ? config.motorRotationSpeed() : -config.motorRotationSpeed()) : (float) 0
                ))
            );
        })).subscribe(eventPublisher::emit, Logger::error);
    }

    private void initToolingMotor(final Joystick joystick) {
        final Observable<ToolingSpeedValue> fwd = joystick.button(config.toolingMotorForwardButton()).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new ToolingSpeedValue(config.motorRotationSpeed());
            }

            return new ToolingSpeedValue(0);
        });
        final Observable<ToolingSpeedValue> rev = joystick.button(config.toolingMotorReverseButton()).map(value -> {
            if (value == Joystick.BUTTON_DOWN) {
                return new ToolingSpeedValue(-config.motorRotationSpeed());
            }

            return new ToolingSpeedValue(0);
        });

        Observable.merge(fwd, rev).subscribe(eventPublisher::emit, Logger::error);
    }
}
