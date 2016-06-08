package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.CpuValue;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class CpuInformationViewController implements ViewController {
    private final Observable<CpuValue> values;

    private final CpuInformationView view;

    private final CompositeSubscription subscriptions;

    @Inject
    public CpuInformationViewController(@Event final Observable<CpuValue> values, final CpuInformationView view) {
        this.values = values;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        subscriptions.add(values.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateLabels));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updateLabels(final CpuValue value) {
        view.frequencyLabel.setText(String.format("%d Hz", value.getFrequency()));
        view.temperatureLabel.setText(String.format("%.1f °C", value.getTemperature()));
        view.voltageLabel.setText(String.format("%.2f V", value.getVoltage()));
    }
}
