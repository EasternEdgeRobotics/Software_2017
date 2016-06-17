package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.value.CurrentValue;
import com.easternedgerobotics.rov.value.DepthValueA;
import com.easternedgerobotics.rov.value.DepthValueB;
import com.easternedgerobotics.rov.value.ExternalPressureValueA;
import com.easternedgerobotics.rov.value.ExternalPressureValueB;
import com.easternedgerobotics.rov.value.ExternalTemperatureValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.VoltageValue;

import javafx.scene.control.Label;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class SensorViewController implements ViewController {
    private static final float BUS_LINE_48 = 48;

    private static final float BUS_LINE_12 = 12;

    private static final float BUS_LINE_05 =  5;

    private static final int PRESSURE_BUFFER_WINDOW_DURATION = 500;

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

    private final Observable<DepthValueA> depthA;

    private final Observable<DepthValueB> depthB;

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
        @Event final Observable<DepthValueA> depthA,
        @Event final Observable<DepthValueB> depthB,
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
        this.depthA = depthA;
        this.depthB = depthB;
        this.internalTemperature = internalTemperature;
        this.externalTemperature = externalTemperature;
        this.voltage = voltage;
        this.current = current;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().add(cpuInformationView.getParent());
        subscriptions.add(internalPressure.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(
            externalPressureA
                .observeOn(Schedulers.computation())
                .buffer(PRESSURE_BUFFER_WINDOW_DURATION, TimeUnit.MILLISECONDS)
                .filter(x -> !x.isEmpty())
                .map(values -> new ExternalPressureValueA((float) values.stream()
                    .mapToDouble(ExternalPressureValueA::getValue)
                    .summaryStatistics()
                    .getAverage()))
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(this::updatePressureLabel));
        subscriptions.add(
            externalPressureB
                .observeOn(Schedulers.computation())
                .buffer(PRESSURE_BUFFER_WINDOW_DURATION, TimeUnit.MILLISECONDS)
                .filter(x -> !x.isEmpty())
                .map(values -> new ExternalPressureValueB((float) values.stream()
                    .mapToDouble(ExternalPressureValueB::getValue)
                    .summaryStatistics()
                    .getAverage()))
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(this::updatePressureLabel));
        subscriptions.add(depthA.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateDepthLabelA));
        subscriptions.add(depthB.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateDepthLabelB));
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

    private void updateDepthLabelA(final DepthValueA value) {
        view.depthLabelA.setText(String.format(SensorView.DEPTH_LABEL_FORMAT, value.getValue()));
    }

    private void updateDepthLabelB(final DepthValueB value) {
        view.depthLabelB.setText(String.format(SensorView.DEPTH_LABEL_FORMAT, value.getValue()));
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
