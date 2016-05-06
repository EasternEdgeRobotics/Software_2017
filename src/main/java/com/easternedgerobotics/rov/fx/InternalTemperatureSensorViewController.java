package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class InternalTemperatureSensorViewController implements ViewController {
    private final Observable<InternalTemperatureValue> internalTemperature;

    private final InternalTemperatureSensorView view;

    private CompositeSubscription subscriptions;

    @Inject
    public InternalTemperatureSensorViewController(
        @Event
        final Observable<InternalTemperatureValue> internalTemperature,
        final InternalTemperatureSensorView view
    ) {
        this.internalTemperature = internalTemperature;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(internalTemperature.subscribe(this::updateInternalTemperatureLabel));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updateInternalTemperatureLabel(final InternalTemperatureValue internalTemperatureValue) {
        view.sensorValueLabel.setText(String.format("%.1f °C", internalTemperatureValue.getInternalTemperature()));
    }
}
