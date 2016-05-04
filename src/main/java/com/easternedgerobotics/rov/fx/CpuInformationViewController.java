package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.CpuValue;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class CpuInformationViewController implements ViewController {
    private final Observable<CpuValue> rasprime;

    private final CpuInformationView view;

    private final CompositeSubscription subscriptions;

    @Inject
    public CpuInformationViewController(@Event final Observable<CpuValue> rasprime, final CpuInformationView view) {
        this.rasprime = rasprime;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        view.frequencyLabel.setText("???");
        view.temperatureLabel.setText("???");
        view.voltageLabel.setText("???");
        subscriptions.add(rasprime.subscribe(this::updateLabels));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updateLabels(final CpuValue value) {
        view.frequencyLabel.setText(Long.toString(value.getFrequency()));
        view.temperatureLabel.setText(Float.toString(value.getTemperature()));
        view.voltageLabel.setText(Float.toString(value.getVoltage()));
    }
}
