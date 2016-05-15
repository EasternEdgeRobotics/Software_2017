package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.SensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;
import com.easternedgerobotics.rov.value.SpeedValue;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.Observable;

import java.util.HashMap;

public final class Topside extends Application {
    private static final String AFT_CAMERA_MOTOR_NAME = "AftCamera";

    private static final int AFT_CAMERA_MOTOR_FORWARD_JOYSTICK_BUTTON = 4;

    private static final int AFT_CAMERA_MOTOR_REVERSE_JOYSTICK_BUTTON = 6;

    private static final float AFT_CAMERA_MOTOR_ROTATION_SPEED = 0.75f;

    private EventPublisher eventPublisher;

    private ViewLoader viewLoader;

    @Override
    public void init() {
        eventPublisher = new UdpEventPublisher("192.168.88.255");
        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
            }
        });

        Joysticks.logitechExtreme3dPro().subscribe(this::joystickInitialization);

        Logger.info("Initialised");
    }

    @Override
    public void start(final Stage stage) {
        Logger.info("Starting");

        viewLoader.loadIntoStage(MainView.class, stage);
        stage.setTitle("Control Software");
        stage.show();

        final Stage thrusterStage = viewLoader.load(ThrusterPowerSlidersView.class);
        thrusterStage.setTitle("Thruster Power");
        thrusterStage.initOwner(stage);
        thrusterStage.show();

        final Stage sensorStage = viewLoader.load(SensorView.class);
        sensorStage.setTitle("Sensors 'n' stuff");
        sensorStage.initOwner(stage);
        sensorStage.show();

        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        eventPublisher.stop();
        Logger.info("Stopped");
    }

    /**
     * Initializes the given joystick.
     * @param joystick the joystick
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    private void joystickInitialization(final Joystick joystick) {
        final ExponentialMotionScale scale = new ExponentialMotionScale();
        final Observable<SpeedValue> aftCameraForward = joystick.button(AFT_CAMERA_MOTOR_FORWARD_JOYSTICK_BUTTON)
            .map(value -> new SpeedValue(AFT_CAMERA_MOTOR_NAME, value ?  AFT_CAMERA_MOTOR_ROTATION_SPEED : 0));
        final Observable<SpeedValue> aftCameraReverse = joystick.button(AFT_CAMERA_MOTOR_REVERSE_JOYSTICK_BUTTON)
            .map(value -> new SpeedValue(AFT_CAMERA_MOTOR_NAME, value ? -AFT_CAMERA_MOTOR_ROTATION_SPEED : 0));
        joystick.axes().map(scale::apply).subscribe(eventPublisher::emit, Logger::error);
        aftCameraForward.mergeWith(aftCameraReverse)
            .subscribe(eventPublisher::emit, Logger::error);
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
