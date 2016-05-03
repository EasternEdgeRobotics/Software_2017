package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.PressureSensorView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;

import java.util.HashMap;

public final class Topside extends Application {
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

        final ExponentialMotionScale scale = new ExponentialMotionScale();
        Joysticks.logitechExtreme3dPro().flatMap(Joystick::axes)
            .map(scale::apply)
            .subscribe(eventPublisher::emit, Logger::error);

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

        final Stage pressureStage = viewLoader.load(PressureSensorView.class);
        pressureStage.setTitle("Pressure Sensors");
        pressureStage.initOwner(stage);
        pressureStage.show();

        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        eventPublisher.stop();
        Logger.info("Stopped");
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
