package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.Config;
import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.TopsidesConfig;
import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.SensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.MotionPowerProfile;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.io.joystick.JoystickController;
import com.easternedgerobotics.rov.io.joystick.LogitechExtremeJoystickSource;
import com.easternedgerobotics.rov.value.LightSpeedValue;
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
    private static final float MAX_SLIDER_VALUE = 100f;

    private static final long JOYSTICK_RECOVERY_INTERVAL = 1000;

    private EventPublisher eventPublisher;

    private PilotPanel pilotPanel;

    private VideoPlayer videoPlayer;

    private ViewLoader viewLoader;

    private MotionPowerProfile profile;

    @Override
    public void init() throws SocketException, UnknownHostException {
        final LaunchConfig launchConfig = new Config(
            getParameters().getNamed().get("default"),
            getParameters().getNamed().get("config")
        ).getConfig("launch", LaunchConfig.class);
        final TopsidesConfig config = new Config(
            getParameters().getNamed().get("default"),
            getParameters().getNamed().get("config")
        ).getConfig("topsides", TopsidesConfig.class);
        final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
        final int broadcastPort = launchConfig.defaultBroadcastPort();
        final DatagramSocket socket = new DatagramSocket(broadcastPort);
        eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
        pilotPanel = new PilotPanel(config.pilotPanelName(), config.pilotPanelPort());
        profile = new MotionPowerProfile(config.profilePref());
        videoPlayer = new VideoPlayer(eventPublisher, config.mpv());
        viewLoader = new ViewLoader(new HashMap<Class<?>, Object>() {
            {
                put(EventPublisher.class, eventPublisher);
                put(PilotPanel.class, pilotPanel);
                put(MotionPowerProfile.class, profile);
            }
        });

        LogitechExtremeJoystickSource.create(
            JOYSTICK_RECOVERY_INTERVAL,
            TimeUnit.MILLISECONDS,
            Schedulers.io()
            ).subscribe(new JoystickController(eventPublisher, new ExponentialMotionScale())::onNext);
        pilotPanel.lightPowerSlider().map(value -> value / MAX_SLIDER_VALUE)
            .map(LightSpeedValue::new).subscribe(eventPublisher::emit);

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

    public static void main(final String[] args) {
        launch(args);
    }
}
