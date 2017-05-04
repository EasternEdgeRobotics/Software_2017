package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.Config;
import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.SliderConfig;
import com.easternedgerobotics.rov.config.TopsidesConfig;
import com.easternedgerobotics.rov.config.VideoDecoderConfig;
import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.ViewLauncher;
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
import com.easternedgerobotics.rov.video.VideoDecoder;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public final class Topside extends Application {
    private TopsidesConfig config;

    private EventPublisher eventPublisher;

    private ViewLauncher launcher;

    private ViewLoader viewLoader;

    private Arduino arduino;

    private VideoDecoder videoDecoder;

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

        arduino = new Arduino(
            new ArduinoPort(
                config.pilotPanelName(),
                config.pilotPanelPort(),
                config.pilotPanelTimeOut(),
                config.pilotPanelBaud()),
            new byte[0],
            config.pilotPanelOutputs(),
            config.pilotPanelInputPullups());

        final MotionPowerProfile profiles = new MotionPowerProfile(config.profilePref());

        final EmergencyStopController emergencyStopController = new EmergencyStopController(
            arduino, config.emergencyStopButtonAddress());

        final SliderController sliderController = new SliderController(
            arduino,
            Schedulers.io(),
            eventPublisher.valuesOfType(MotionPowerValue.class),
            configSource.getConfig("slider", SliderConfig.class));
        sliderController.getMotion().subscribe(eventPublisher::emit);
        sliderController.getLights().subscribe(eventPublisher::emit);

        final ProfileController profileController = new ProfileController(
            arduino, config.pilotPanelInputPullups(), config.pilotPanelOutputs(), config.profileSwitchDuration(),
            profiles, eventPublisher.valuesOfType(MotionPowerValue.class), Schedulers.io());
        profileController.getMotion().subscribe(eventPublisher::emit);

        videoDecoder = new VideoDecoder(
            eventPublisher, configSource.getConfig("videoDecoder", VideoDecoderConfig.class));

        launcher = new ViewLauncher();
        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
                put(EmergencyStopController.class, emergencyStopController);
                put(VideoDecoder.class, videoDecoder);
                put(ViewLauncher.class, launcher);
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
    }

    @Override
    public void start(final Stage stage) {
        Logger.info("Starting");

        launcher.start(viewLoader, stage, MainView.class, "Control Software");
        arduino.start(config.pilotPanelHeartbeatInterval(), config.pilotPanelHeartbeatTimeout(), TimeUnit.MILLISECONDS);
        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        eventPublisher.stop();
        arduino.stop();
        videoDecoder.stop();
        Logger.info("Stopped");
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
