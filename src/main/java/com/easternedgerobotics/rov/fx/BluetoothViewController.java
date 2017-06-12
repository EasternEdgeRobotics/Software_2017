package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.BluetoothValue;

import rx.Observable;
import rx.Subscription;

import javax.inject.Inject;

public class BluetoothViewController implements ViewController {
    private final BluetoothView view;

    private final Observable<BluetoothValue> bluetoothValues;

    private Subscription subscription;

    @Inject
    public BluetoothViewController(
        final BluetoothView view,
        @Event final Observable<BluetoothValue> bluetoothValues
    ) {
        this.view = view;
        this.bluetoothValues = bluetoothValues;
    }

    @Override
    public final void onCreate() {
        subscription = bluetoothValues.observeOn(JAVA_FX_SCHEDULER).subscribe(this::addBluetoothValue);
    }

    @Override
    public final void onDestroy() {
        subscription.unsubscribe();
    }

    private void addBluetoothValue(final BluetoothValue value) {
        view.addValue(value.getMessage());
    }
}
