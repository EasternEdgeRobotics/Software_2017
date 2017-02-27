package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.HeartbeatValue;

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

    private final EventPublisher eventPublisher;

    private final PilotPanel pilotPanel;

    private final CompositeSubscription subscriptions;

    @Inject
    public MainViewController(final MainView view, final EventPublisher eventPublisher, final PilotPanel pilotPanel) {
        this.view = view;
        this.eventPublisher = eventPublisher;
        this.pilotPanel = pilotPanel;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            Observable.interval(HEARTBEAT_GAP, TimeUnit.SECONDS, Schedulers.io())
                .withLatestFrom(
                    JavaFxObservable.valuesOf(view.button.selectedProperty()).startWith(false),
                    (tick, beat) -> beat)
                .subscribe(this::heartbeat));
        subscriptions.add(JavaFxObservable.valuesOf(view.button.selectedProperty())
            .subscribe(this::onSelected));
        subscriptions.add(pilotPanel.emergencyStopClick().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(this::onEmergencyStopClick));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
        eventPublisher.emit(new HeartbeatValue(false));
    }

    private void onSelected(final boolean selected) {
        if (selected) {
            view.button.setText("Stop");
        } else {
            view.button.setText("Start");
        }
    }

    private void heartbeat(final boolean operational) {
        eventPublisher.emit(new HeartbeatValue(operational));
    }

    private void onEmergencyStopClick(final boolean stop) {
        if (!stop) {
            view.button.setDisable(true);
            view.button.setSelected(false);
            view.button.setStyle("-fx-text-fill: red");
            view.button.setText("Emergency Stop");
        } else {
            view.button.setDisable(false);
            view.button.setStyle("-fx-text-fill: black");
            view.button.setText("Start");
        }
    }
}
