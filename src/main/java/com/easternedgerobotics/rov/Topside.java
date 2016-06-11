package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.control.MotionReverser;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.SensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;
import com.easternedgerobotics.rov.io.MotionPowerProfile;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.CameraSpeedValueA;
import com.easternedgerobotics.rov.value.CameraSpeedValueB;
import com.easternedgerobotics.rov.value.LightSpeedValue;
import com.easternedgerobotics.rov.value.ToolingSpeedValue;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoFlipValueB;
import com.easternedgerobotics.rov.video.VideoPlayer;

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
    private static final int CAMERA_A_MOTOR_FORWARD_JOYSTICK_BUTTON = 4;

    private static final int CAMERA_A_MOTOR_REVERSE_JOYSTICK_BUTTON = 6;

    private static final int CAMERA_B_MOTOR_FORWARD_JOYSTICK_BUTTON = 5;

    private static final int CAMERA_B_MOTOR_REVERSE_JOYSTICK_BUTTON = 3;

    private static final int TOOLING_MOTOR_FORWARD_JOYSTICK_BUTTON = 11;

    private static final int TOOLING_MOTOR_REVERSE_JOYSTICK_BUTTON = 12;

    private static final float MOTOR_ROTATION_SPEED = 0.3f;

    private static final int MOTION_REVERSE_JOYSTICK_BUTTON = 2;

    private static final int CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON = 7;

    private static final int CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON = 8;

    private static final float MAX_SLIDER_VALUE = 100f;

    private EventPublisher eventPublisher;

    private PilotPanel pilotPanel;

    private VideoPlayer videoPlayer;

    private ViewLoader viewLoader;

    private MotionPowerProfile profile;

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
        profile = new MotionPowerProfile(
            System.getProperty("profile-pref", "profiles"));
        videoPlayer = new VideoPlayer(eventPublisher, System.getProperty("mpv", "192.168.88.2"));
        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
                put(PilotPanel.class, pilotPanel);
                put(MotionPowerProfile.class, profile);
            }
        });

        Joysticks.logitechExtreme3dPro().subscribe(this::joystickInitialization);
        pilotPanel.lightPowerSlider().map(value -> value / MAX_SLIDER_VALUE)
            .map(LightSpeedValue::new).subscribe(eventPublisher::emit);

        Logger.info("Initialising video player");
        videoPlayer.init();
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
        videoPlayer.stop();
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
        final MotionReverser reverser = new MotionReverser();
        joystick.button(CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON).filter(x -> x).map(x -> new VideoFlipValueA())
            .doOnEach(Logger::info)
            .subscribe(eventPublisher::emit);
        joystick.button(CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON).filter(x -> x).map(x -> new VideoFlipValueB())
            .doOnEach(Logger::info)
            .subscribe(eventPublisher::emit);
        joystick.button(MOTION_REVERSE_JOYSTICK_BUTTON).filter(x -> x).subscribe(press -> reverser.toggle());
        joystick.axes().map(scale::apply).map(reverser::apply).subscribe(eventPublisher::emit, Logger::error);

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

    public static void main(final String[] args) {
        launch(args);
    }
}
