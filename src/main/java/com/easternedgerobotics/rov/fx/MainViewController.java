package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.EmergencyStopController;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.PicameraAHeartbeatValue;
import com.easternedgerobotics.rov.value.PicameraBHeartbeatValue;
import com.easternedgerobotics.rov.value.RasprimeHeartbeatValue;
import com.easternedgerobotics.rov.value.TopsideHeartbeatValue;
import com.easternedgerobotics.rov.video.VideoDecoder;

import javafx.scene.control.ToggleButton;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class MainViewController implements ViewController {
    private final MainView view;

    private final ViewLoader viewLoader;

    private final VideoDecoder videoDecoder;

    private final EventPublisher eventPublisher;

    private final EmergencyStopController emergencyStopController;

    private final int heartbeatRate;

    private final int maxHeartbeatGap;

    private final CompositeSubscription subscriptions;

    @Inject
    public MainViewController(
        final MainView view,
        final ViewLoader viewLoader,
        final VideoDecoder videoDecoder,
        final EventPublisher eventPublisher,
        final EmergencyStopController emergencyStopController,
        @Configurable("launch.heartbeatRate") final int heartbeatRate,
        @Configurable("topsides.heartbeatLostInterval") final int maxHeartbeatGap
    ) {
        this.view = view;
        this.viewLoader = viewLoader;
        this.videoDecoder = videoDecoder;
        this.eventPublisher = eventPublisher;
        this.emergencyStopController = emergencyStopController;
        this.heartbeatRate = heartbeatRate;
        this.maxHeartbeatGap = maxHeartbeatGap;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            Observable.interval(heartbeatRate, TimeUnit.SECONDS, Schedulers.io())
                .withLatestFrom(
                    JavaFxObservable.valuesOf(view.startButton.selectedProperty()).startWith(false),
                    (tick, beat) -> beat)
                .subscribe(this::heartbeat));
        subscriptions.add(JavaFxObservable.valuesOf(view.startButton.selectedProperty())
            .subscribe(this::onSelected));
        subscriptions.add(emergencyStopController.emergencyStop().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(this::onEmergencyStopClick));

        final Observable<Long> heartbeatLostInterval = Observable.interval(
            maxHeartbeatGap, TimeUnit.SECONDS, JAVA_FX_SCHEDULER);

        final Observable<HeartbeatValue> rasprimeHeartbeats = eventPublisher
            .valuesOfType(RasprimeHeartbeatValue.class).cast(HeartbeatValue.class);
        final Observable<HeartbeatValue> picameraAHeartbeats = eventPublisher
            .valuesOfType(PicameraAHeartbeatValue.class).cast(HeartbeatValue.class);
        final Observable<HeartbeatValue> picameraBHeartbeats = eventPublisher
            .valuesOfType(PicameraBHeartbeatValue.class).cast(HeartbeatValue.class);

        subscriptions.addAll(
            setIndicator(rasprimeHeartbeats, view.rasprimeIndicator),
            setIndicator(picameraAHeartbeats, view.picameraAIndicator),
            setIndicator(picameraBHeartbeats, view.picameraBIndicator));

        JavaFxObservable.valuesOf(view.thrusterButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLoader.load(ThrusterPowerSlidersView.class, "Thruster Power"));
        JavaFxObservable.valuesOf(view.sensorButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLoader.load(SensorView.class, "Sensors 'n' stuff"));
        JavaFxObservable.valuesOf(view.cameraButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLoader.load(VideoView.class, "Cameras"));
        JavaFxObservable.valuesOf(view.resetCameraButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> videoDecoder.restart());
        JavaFxObservable.valuesOf(view.distanceButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLoader.load(DistanceCalculatorView.class, "Distance Calculator"));
        JavaFxObservable.valuesOf(view.calibrationButton.pressedProperty()).filter(x -> !x)
            .subscribe(v -> viewLoader.load(CameraCalibrationView.class, "California Camera Calibration by Cal"));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
        eventPublisher.emit(new TopsideHeartbeatValue(false));
    }

    private Subscription setIndicator(final Observable<HeartbeatValue> heartbeats, final ToggleButton indicator) {
        final Observable<Boolean> timeout = Observable.just(false)
            .delay(maxHeartbeatGap, TimeUnit.SECONDS, JAVA_FX_SCHEDULER)
            .concatWith(Observable.never());
        return new CompositeSubscription(
            timeout.takeUntil(heartbeats).repeat().subscribe(h -> indicator.setBackground(MainView.LOST_BG)),
            heartbeats.subscribe(h -> indicator.setBackground(MainView.FOUND_BG)));
    }

    private void onSelected(final boolean selected) {
        if (selected) {
            view.startButton.setText("Stop");
        } else {
            view.startButton.setText("Start");
        }
    }

    private void heartbeat(final boolean operational) {
        eventPublisher.emit(new TopsideHeartbeatValue(operational));
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
