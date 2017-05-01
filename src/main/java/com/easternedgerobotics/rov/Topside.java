package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.Config;
import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.TopsidesConfig;
import com.easternedgerobotics.rov.config.VideoPlayerConfig;
import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.SensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.EmergencyStopController;
import com.easternedgerobotics.rov.io.MotionPowerProfile;
import com.easternedgerobotics.rov.io.ProfileController;
import com.easternedgerobotics.rov.io.SliderController;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.io.arduino.ArduinoPort;
import com.easternedgerobotics.rov.io.joystick.JoystickController;
import com.easternedgerobotics.rov.io.joystick.LogitechExtremeJoystickSource;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.video.VideoPlayer;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class Topside extends Application {
    private TopsidesConfig config;

    private EventPublisher eventPublisher;

    private VideoPlayer videoPlayer;

    private ViewLoader viewLoader;

    private Arduino arduino;

    @Override
    public void init() throws SocketException, UnknownHostException {
        final Config configSource = new Config(
            getParameters().getNamed().get("default"),
            getParameters().getNamed().get("config")
        );
        final LaunchConfig launchConfig = configSource.getConfig("launch", LaunchConfig.class);
        config = configSource.getConfig("topsides", TopsidesConfig.class);

        final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
        final int broadcastPort = launchConfig.defaultBroadcastPort();
        final DatagramSocket socket = new DatagramSocket(broadcastPort);
        eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));

        videoPlayer = new VideoPlayer(
            eventPublisher,
            config.mpv(),
            configSource.getConfig("videoPlayer", VideoPlayerConfig.class)
        );

        arduino = new Arduino(
            new ArduinoPort(
                config.pilotPanelName(),
                config.pilotPanelPort(),
                config.pilotPanelTimeOut(),
                config.pilotPanelBaud()),
            config.pilotPanelInputs(),
            config.pilotPanelOutputs(),
            config.pilotPanelInputPullups());

        final MotionPowerProfile profiles = new MotionPowerProfile(config.profilePref());

        final EmergencyStopController emergencyStopController = new EmergencyStopController(
            arduino, config.emergencyStopButtonAddress());

        final SliderController sliderController = new SliderController(
            arduino, Schedulers.io(), eventPublisher.valuesOfType(MotionPowerValue.class));
        sliderController.getMotion().subscribe(eventPublisher::emit);
        sliderController.getLights().subscribe(eventPublisher::emit);

        final ProfileController profileController = new ProfileController(
            arduino, config.pilotPanelInputs(), config.pilotPanelOutputs(), config.profileSwitchDuration(),
            profiles, eventPublisher.valuesOfType(MotionPowerValue.class), Schedulers.io());
        profileController.getMotion().subscribe(eventPublisher::emit);

        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
                put(EmergencyStopController.class, emergencyStopController);
            }
        });

        LogitechExtremeJoystickSource.create(
            config.joystickRecoveryInterval(),
            TimeUnit.MILLISECONDS,
            Schedulers.io()
        ).subscribe(new JoystickController(
            eventPublisher,
            new ExponentialMotionScale(),
            configSource.getConfig("joystick", JoystickConfig.class)
        )::onNext);

        try {
            Logger.info("Initialising video player");
            videoPlayer.init();
        } catch (final RuntimeException e) {
            if (!(e.getCause() instanceof IOException)) {
                throw e;
            }

            Logger.warn("The video player could not be initialised");
            Logger.warn(e);
        }
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

        arduino.start(config.pilotPanelHeartbeatInterval(), config.pilotPanelHeartbeatTimeout(), TimeUnit.MILLISECONDS);
        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        videoPlayer.stop();
        eventPublisher.stop();
        arduino.stop();
        Logger.info("Stopped");
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
