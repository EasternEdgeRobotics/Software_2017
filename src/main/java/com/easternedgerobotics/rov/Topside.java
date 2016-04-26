package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.ExponentialMotionScale;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.fx.MainView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ViewLoader;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import javafx.application.Application;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;

public final class Topside extends Application {
    private EventPublisher eventPublisher;

    private Injector injector;

    @Override
    public void init() {
        eventPublisher = new UdpEventPublisher("192.168.88.255");
        injector = Guice.createInjector(
            binder -> binder.bind(EventPublisher.class).toProvider(() -> eventPublisher).in(Singleton.class),
            Binder::requireAtInjectOnConstructors);

        final ExponentialMotionScale scale = new ExponentialMotionScale();
        Joysticks.logitechExtreme3dPro().flatMap(Joystick::axes)
            .map(scale::apply)
            .subscribe(eventPublisher::emit, Logger::error);

        Logger.info("Initialised");
    }

    @Override
    public void start(final Stage stage) {
        Logger.info("Starting");
        final ViewLoader viewLoader = injector.getInstance(ViewLoader.class);

        viewLoader.loadIntoStage(MainView.class, stage);
        stage.setTitle("Control Software");
        stage.show();

        final Stage thrusterStage = viewLoader.load(ThrusterPowerSlidersView.class);
        thrusterStage.setTitle("Thruster Power");
        thrusterStage.initOwner(stage);
        thrusterStage.show();

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
