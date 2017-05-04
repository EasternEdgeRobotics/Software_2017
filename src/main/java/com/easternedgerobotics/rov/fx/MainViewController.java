package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.EmergencyStopController;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.video.VideoDecoder;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class MainViewController implements ViewController {
    private static final int HEARTBEAT_GAP = 1;

    private final MainView view;

    private final ViewLauncher viewLauncher;

    private final VideoDecoder videoDecoder;

    private final EventPublisher eventPublisher;

    private final EmergencyStopController emergencyStopController;

    private final CompositeSubscription subscriptions;

    @Inject
    public MainViewController(
        final MainView view,
        final ViewLauncher viewLauncher,
        final VideoDecoder videoDecoder,
        final EventPublisher eventPublisher,
        final EmergencyStopController emergencyStopController
    ) {
        this.view = view;
        this.viewLauncher = viewLauncher;
        this.videoDecoder = videoDecoder;
        this.eventPublisher = eventPublisher;
        this.emergencyStopController = emergencyStopController;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            Observable.interval(HEARTBEAT_GAP, TimeUnit.SECONDS, Schedulers.io())
                .withLatestFrom(
                    JavaFxObservable.valuesOf(view.startButton.selectedProperty()).startWith(false),
                    (tick, beat) -> beat)
                .subscribe(this::heartbeat));
        subscriptions.add(JavaFxObservable.valuesOf(view.startButton.selectedProperty())
            .subscribe(this::onSelected));
        subscriptions.add(emergencyStopController.emergencyStop().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(this::onEmergencyStopClick));

        JavaFxObservable.valuesOf(view.thrusterButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLauncher.launch(ThrusterPowerSlidersView.class, "Thruster Power"));
        JavaFxObservable.valuesOf(view.sensorButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLauncher.launch(SensorView.class, "Sensors 'n' stuff"));
        JavaFxObservable.valuesOf(view.cameraButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLauncher.launch(VideoView.class, "Cameras"));
        JavaFxObservable.valuesOf(view.resetCameraButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> videoDecoder.restart());
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
        eventPublisher.emit(new HeartbeatValue(false));
    }

    private void onSelected(final boolean selected) {
        if (selected) {
            view.startButton.setText("Stop");
        } else {
            view.startButton.setText("Start");
        }
    }

    private void heartbeat(final boolean operational) {
        eventPublisher.emit(new HeartbeatValue(operational));
    }

    private void onEmergencyStopClick(final boolean stop) {
        if (!stop) {
            view.startButton.setDisable(true);
            view.startButton.setSelected(false);
            view.startButton.setStyle("-fx-text-fill: red");
            view.startButton.setText("Emergency Stop");
        } else {
            view.startButton.setDisable(false);
            view.startButton.setStyle("-fx-text-fill: black");
            view.startButton.setText("Start");
        }
    }
}
