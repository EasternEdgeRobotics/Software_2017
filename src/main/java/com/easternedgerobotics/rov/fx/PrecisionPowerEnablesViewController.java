package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.PrecisionPowerValue;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class PrecisionPowerEnablesViewController implements ViewController {
    /**
     * Used to display convert power value to percentage.
     */
    static final float MAX_VALUE = 100f;

    /**
     * The event publisher instance.
     */
    final EventPublisher eventPublisher;

    /**
     * The pilot panel interface.
     */
    final PilotPanel pilotPanel;

    /**
     * The @{code PrecisionPowerEnablesView} this controller works for.
     */
    final PrecisionPowerEnablesView view;

    /**
     * The enable for precision power in the heave direction.
     */
    final EnableView heaveEnableView;

    /**
     * The enable for precision power in the sway direction.
     */
    final EnableView swayEnableView;

    /**
     * The enable for precision power in the surge direction.
     */
    final EnableView surgeEnableView;

    /**
     * The enable for precision power in the pitch direction.
     */
    final EnableView pitchEnableView;

    /**
     * The enable for precision power in the yaw direction.
     */
    final EnableView yawEnableView;

    /**
     * The enable for precision power in the roll direction.
     */
    final EnableView rollEnableView;

    /**
     * The subscriptions held by this view controller.
     */
    final CompositeSubscription subscriptions;

    @Inject
    public PrecisionPowerEnablesViewController(
        final EventPublisher eventPublisher,
        final PilotPanel pilotPanel,
        final PrecisionPowerEnablesView view,
        final EnableView heaveEnableView,
        final EnableView swayEnableView,
        final EnableView surgeEnableView,
        final EnableView pitchEnableView,
        final EnableView yawEnableView,
        final EnableView rollEnableView
    ) {
        this.eventPublisher = eventPublisher;
        this.pilotPanel = pilotPanel;
        this.view = view;
        this.heaveEnableView = heaveEnableView;
        this.swayEnableView = swayEnableView;
        this.surgeEnableView = surgeEnableView;
        this.pitchEnableView = pitchEnableView;
        this.yawEnableView = yawEnableView;
        this.rollEnableView = rollEnableView;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        heaveEnableView.displayNameLabel.setText("Heave");
        swayEnableView.displayNameLabel.setText("Sway");
        surgeEnableView.displayNameLabel.setText("Surge");
        yawEnableView.displayNameLabel.setText("Yaw");
        rollEnableView.displayNameLabel.setText("Roll");

        view.row.getChildren().addAll(
            heaveEnableView.getParent(),
            swayEnableView.getParent(),
            surgeEnableView.getParent(),
            yawEnableView.getParent(),
            rollEnableView.getParent()
        );

        final Observable<Integer> power = pilotPanel.precisionPowerSlider();
        final Observable<Boolean> heave = values(heaveEnableView);
        final Observable<Boolean> sway = values(swayEnableView);
        final Observable<Boolean> surge = values(surgeEnableView);
        final Observable<Boolean> pitch = values(pitchEnableView);
        final Observable<Boolean> yaw = values(yawEnableView);
        final Observable<Boolean> roll = values(rollEnableView);

        subscriptions.add(Observable
            .combineLatest(
                power.map(value -> value / MAX_VALUE), heave, sway, surge, pitch, yaw, roll, PrecisionPowerValue::new)
            .observeOn(Schedulers.io())
            .subscribe(eventPublisher::emit));

        subscriptions.add(Observable
            .combineLatest(Observable.just(PrecisionPowerEnablesView.POWER_LABEL_FORMAT), power, String::format)
            .subscribe(view.powerLabel::setText));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private Observable<Boolean> values(final EnableView enableView) {
        return JavaFxObservable.fromObservableValue(enableView.enable.selectedProperty());
    }
}
