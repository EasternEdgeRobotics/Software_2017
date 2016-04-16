package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.HeartbeatValue;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class MainViewController implements ViewController {
    private static final int HEARTBEAT_GAP = 1;

    private final MainView view;

    private final EventPublisher eventPublisher;

    private final CompositeSubscription subscriptions;

    @Inject
    public MainViewController(final MainView view, final EventPublisher eventPublisher) {
        this.view = view;
        this.eventPublisher = eventPublisher;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            Observable.interval(HEARTBEAT_GAP, TimeUnit.SECONDS, Schedulers.io())
                .withLatestFrom(
                    JavaFxObservable.fromObservableValue(view.button.selectedProperty()).startWith(false),
                    (tick, beat) -> beat)
                .subscribe(this::heartbeat));
        subscriptions.add(JavaFxObservable.fromObservableValue(view.button.selectedProperty())
            .subscribe(this::onSelected));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void onSelected(final boolean selected) {
        if (selected) {
            view.button.setText("Stop");
        } else {
            view.button.setText("Start");
        }
    }

    private void heartbeat(final boolean operational) {
        eventPublisher.emit(HeartbeatValue.create(operational));
    }
}
