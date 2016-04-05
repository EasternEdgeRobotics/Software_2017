package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersView;
import com.easternedgerobotics.rov.fx.ThrusterPowerSlidersViewController;
import com.easternedgerobotics.rov.fx.ViewController;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public final class Topside extends Application {
    /**
     * The duration between heartbeat broadcasts.
     */
    private static final long HEARTBEAT_GAP = 5;

    private EventPublisher eventPublisher;

    @Override
    public void init() {
        eventPublisher = new UdpEventPublisher("192.168.88.255");
        final HeartbeatController heartbeatController = new HeartbeatController(
            eventPublisher, Observable.interval(HEARTBEAT_GAP, TimeUnit.MILLISECONDS, Schedulers.io()));
        final Observable<Joystick> joystick = Joysticks.logitechExtreme3dPro();

        Joysticks.logitechExtreme3dPro().flatMap(Joystick::axes)
            .subscribe(eventPublisher::emit, Logger::error);
        heartbeatController.start();
        Logger.info("Initialised");
    }

    @Override
    public void start(final Stage stage) {
        final ThrusterPowerSlidersView view = new ThrusterPowerSlidersView();
        final ViewController viewController = new ThrusterPowerSlidersViewController(eventPublisher, view);
        final Scene window = new Scene(view.getParent(), 600, 400);

        stage.setTitle("Thruster Power Control");
        stage.setScene(window);

        viewController.onCreate();

        stage.show();
        Logger.info("Started");
    }

    @Override
    public void stop() {
        Logger.info("Stopping");
        eventPublisher.stop();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
