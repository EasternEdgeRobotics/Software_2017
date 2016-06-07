package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.InternalPressureValue;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

class InternalPressureSensorViewController implements ViewController {
    private final Observable<InternalPressureValue> internalPressure;

    private final InternalPressureSensorView view;

    private CompositeSubscription subscriptions;

    @Inject
    public InternalPressureSensorViewController(
        @Event
        final Observable<InternalPressureValue> internalPressure,
        final InternalPressureSensorView view
    ) {
        this.internalPressure = internalPressure;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(
            internalPressure.observeOn(jfxScheduler)
                .subscribe(this::updatePressureLabel));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updatePressureLabel(final InternalPressureValue value) {
        view.sensorValueLabel.setText(String.format(
            InternalPressureSensorView.PRESSURE_LABEL_FORMAT, value.getPressure()));
    }
}
