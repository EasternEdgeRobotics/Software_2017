package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.SensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.AftCameraSpeedValue;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

public final class Topside extends Application {
    private static final int AFT_CAMERA_MOTOR_FORWARD_JOYSTICK_BUTTON = 6;

    private static final int AFT_CAMERA_MOTOR_REVERSE_JOYSTICK_BUTTON = 4;

    private static final float AFT_CAMERA_MOTOR_ROTATION_SPEED = 0.3f;

    private EventPublisher eventPublisher;

    private PilotPanel pilotPanel;

    private ViewLoader viewLoader;

    @Override
    public void init() throws SocketException, UnknownHostException {
        final InetAddress broadcastAddress = InetAddress.getByName(System.getProperty("broadcast", "192.168.88.255"));
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        final DatagramSocket socket = new DatagramSocket(broadcastPort);
        eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
        pilotPanel = new PilotPanel(
            System.getProperty("pilot-panel-name", "Pilot Panel"),
            System.getProperty("pilot-panel-port", "/dev/ttyACM0"));
        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
                put(PilotPanel.class, pilotPanel);
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

        pilotPanel.start();
        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        eventPublisher.stop();
        pilotPanel.stop();
        Logger.info("Stopped");
    }

    /**
     * Initializes the given joystick.
     * @param joystick the joystick
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    private void joystickInitialization(final Joystick joystick) {
        final ExponentialMotionScale scale = new ExponentialMotionScale();
        final Observable<AftCameraSpeedValue> aftCameraForward = joystick
            .button(AFT_CAMERA_MOTOR_FORWARD_JOYSTICK_BUTTON)
            .map(value -> new AftCameraSpeedValue(value ?  AFT_CAMERA_MOTOR_ROTATION_SPEED : 0));
        final Observable<AftCameraSpeedValue> aftCameraReverse = joystick
            .button(AFT_CAMERA_MOTOR_REVERSE_JOYSTICK_BUTTON)
            .map(value -> new AftCameraSpeedValue(value ? -AFT_CAMERA_MOTOR_ROTATION_SPEED : 0));
        joystick.axes().map(scale::apply).subscribe(eventPublisher::emit, Logger::error);
        aftCameraForward.mergeWith(aftCameraReverse)
            .subscribe(eventPublisher::emit, Logger::error);
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
