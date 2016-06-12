package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.CurrentValue;
import com.easternedgerobotics.rov.value.ExternalPressureValueA;
import com.easternedgerobotics.rov.value.ExternalPressureValueB;
import com.easternedgerobotics.rov.value.ExternalTemperatureValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.VoltageValue;

import javafx.scene.control.Label;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class SensorViewController implements ViewController {
    private static final float BUS_LINE_48 = 48;

    private static final float BUS_LINE_12 = 12;

    private static final float BUS_LINE_05 =  5;

    /**
     * The sensor view.
     */
    private final SensorView view;

    /**
     * The CPU information sub-view.
     */
    private final CpuInformationView cpuInformationView;

    private final Observable<InternalPressureValue> internalPressure;

    private final Observable<ExternalPressureValueA> externalPressureA;

    private final Observable<ExternalPressureValueB> externalPressureB;

    private final Observable<InternalTemperatureValue> internalTemperature;

    private final Observable<ExternalTemperatureValue> externalTemperature;

    private final Observable<VoltageValue> voltage;

    private final Observable<CurrentValue> current;

    private final CompositeSubscription subscriptions;

    @Inject
    public SensorViewController(
        final SensorView view,
        final CpuInformationView cpuInformationView,
        @Event final Observable<InternalPressureValue> internalPressure,
        @Event final Observable<ExternalPressureValueA> externalPressureA,
        @Event final Observable<ExternalPressureValueB> externalPressureB,
        @Event final Observable<InternalTemperatureValue> internalTemperature,
        @Event final Observable<ExternalTemperatureValue> externalTemperature,
        @Event final Observable<VoltageValue> voltage,
        @Event final Observable<CurrentValue> current
    ) {
        this.view = view;
        this.cpuInformationView = cpuInformationView;
        this.subscriptions = new CompositeSubscription();

        this.internalPressure = internalPressure;
        this.externalPressureA = externalPressureA;
        this.externalPressureB = externalPressureB;
        this.internalTemperature = internalTemperature;
        this.externalTemperature = externalTemperature;
        this.voltage = voltage;
        this.current = current;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().add(cpuInformationView.getParent());
        subscriptions.add(internalPressure.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(externalPressureA.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(externalPressureB.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(internalTemperature.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateTemperatureLabel));
        subscriptions.add(externalTemperature.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateTemperatureLabel));
        subscriptions.add(
            voltage.filter(value -> value.getBus() == BUS_LINE_48)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateVoltageLabel(view.voltageLabel48, value)));
        subscriptions.add(
            voltage.filter(value -> value.getBus() == BUS_LINE_12)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateVoltageLabel(view.voltageLabel12, value)));
        subscriptions.add(
            voltage.filter(value -> value.getBus() == BUS_LINE_05)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateVoltageLabel(view.voltageLabel05, value)));
        subscriptions.add(
            current.filter(value -> value.getBus() == BUS_LINE_48)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateCurrentLabel(view.currentLabel48, value)));
        subscriptions.add(
            current.filter(value -> value.getBus() == BUS_LINE_12)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateCurrentLabel(view.currentLabel12, value)));
        subscriptions.add(
            current.filter(value -> value.getBus() == BUS_LINE_05)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(value -> updateCurrentLabel(view.currentLabel05, value)));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updatePressureLabel(final InternalPressureValue value) {
        view.internalPressureLabel.setText(String.format(SensorView.PRESSURE_LABEL_FORMAT, value.getPressure()));
    }

    private void updatePressureLabel(final ExternalPressureValueA value) {
        view.externalPressureLabelA.setText(String.format(SensorView.PRESSURE_LABEL_FORMAT, value.getValue()));
    }

    private void updatePressureLabel(final ExternalPressureValueB value) {
        view.externalPressureLabelB.setText(String.format(SensorView.PRESSURE_LABEL_FORMAT, value.getValue()));
    }

    private void updateTemperatureLabel(final InternalTemperatureValue internalTemperatureValue) {
        view.internalTemperatureLabel.setText(
            String.format(SensorView.TEMPERATURE_LABEL_FORMAT, internalTemperatureValue.getTemperature()));
    }

    private void updateTemperatureLabel(final ExternalTemperatureValue internalTemperatureValue) {
        view.externalTemperatureLabel.setText(
            String.format(SensorView.TEMPERATURE_LABEL_FORMAT, internalTemperatureValue.getValue()));
    }

    private void updateVoltageLabel(final Label label, final VoltageValue value) {
        label.setText(String.format(SensorView.VOLTAGE_LABEL_FORMAT, value.getValue()));
    }

    private void updateCurrentLabel(final Label label, final CurrentValue value) {
        label.setText(String.format(SensorView.CURRENT_LABEL_FORMAT, value.getValue()));
    }
}
