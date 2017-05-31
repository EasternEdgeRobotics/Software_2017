package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.io.MPX4250AP;
import com.easternedgerobotics.rov.math.AverageTransformer;
import com.easternedgerobotics.rov.math.MedianTransformer;
import com.easternedgerobotics.rov.value.ExternalPressureValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class SensorViewController implements ViewController {
    private static final float BUS_LINE_48 = 48;

    private static final float BUS_LINE_12 = 12;

    private static final float BUS_LINE_05 =  5;

    private static final int TEMPERATURE_MEDIAN_SAMPLE_SIZE = 25;

    private static final int TEMPERATURE_AVERAGE_SAMPLE_SIZE = 64;

    private static final int SENSOR_MEDIAN_SAMPLE_SIZE = 12;

    private static final int SENSOR_AVERAGE_SAMPLE_SIZE = 32;

    /**
     * The sensor view.
     */
    private final SensorView view;

    /**
     * The CPU information sub-view.
     */
    private final CpuInformationView cpuInformationView;

    private final Observable<InternalPressureValue> internalPressure;

    private final Observable<ExternalPressureValue> externalPressureA;

    private final Observable<InternalTemperatureValue> internalTemperature;

    private final CompositeSubscription subscriptions;

    @Inject
    public SensorViewController(
        final SensorView view,
        final CpuInformationView cpuInformationView,
        @Event final Observable<InternalPressureValue> internalPressure,
        @Event final Observable<ExternalPressureValue> externalPressureA,
        @Event final Observable<InternalTemperatureValue> internalTemperature
    ) {
        this.view = view;
        this.cpuInformationView = cpuInformationView;
        this.subscriptions = new CompositeSubscription();

        this.internalPressure = internalPressure;
        this.externalPressureA = externalPressureA;
        this.internalTemperature = internalTemperature;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().add(cpuInformationView.getParent());
        subscriptions.add(internalPressure.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(
            externalPressureA.map(ExternalPressureValue::getValue)
                .compose(new MedianTransformer<>(SENSOR_MEDIAN_SAMPLE_SIZE))
                .compose(new AverageTransformer<>(SENSOR_AVERAGE_SAMPLE_SIZE))
                .compose(MPX4250AP.CALIBRATION)
                .map(Number::floatValue)
                .map(ExternalPressureValue::new)
                .observeOn(JAVA_FX_SCHEDULER)
                .subscribe(this::updatePressureLabel));
        subscriptions.add(internalTemperature.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updateTemperatureLabel));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private void updatePressureLabel(final InternalPressureValue value) {
        view.internalPressureLabel.setText(String.format(SensorView.PRESSURE_LABEL_FORMAT, value.getPressure()));
    }

    private void updatePressureLabel(final ExternalPressureValue value) {
        view.externalPressureLabel.setText(String.format(SensorView.PRESSURE_LABEL_FORMAT, value.getValue()));
    }

    private void updateTemperatureLabel(final InternalTemperatureValue internalTemperatureValue) {
        view.internalTemperatureLabel.setText(
            String.format(SensorView.TEMPERATURE_LABEL_FORMAT, internalTemperatureValue.getTemperature()));
    }
}
