package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.math.AverageTransformer;
import com.easternedgerobotics.rov.math.MedianTransformer;
import com.easternedgerobotics.rov.value.CpuValue;
import com.easternedgerobotics.rov.value.ExternalPressureValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.PicameraACpuValue;
import com.easternedgerobotics.rov.value.PicameraBCpuValue;
import com.easternedgerobotics.rov.value.RasprimeCpuValue;

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
    private final CpuInformationView rasprimeCpuInformationView;

    private final CpuInformationView picameraACpuInformationView;

    private final CpuInformationView picameraBCpuInformationView;

    private final Observable<RasprimeCpuValue> rasprimeCpuValues;

    private final Observable<PicameraACpuValue> picameraACpuValues;

    private final Observable<PicameraBCpuValue> picameraBCpuValues;

    private final Observable<InternalPressureValue> internalPressure;

    private final Observable<ExternalPressureValue> externalPressureA;

    private final Observable<InternalTemperatureValue> internalTemperature;

    private final CompositeSubscription subscriptions;

    @Inject
    public SensorViewController(
        final SensorView view,
        final CpuInformationView rasprimeCpuInformationView,
        final CpuInformationView picameraACpuInformationView,
        final CpuInformationView picameraBCpuInformationView,
        @Event final Observable<RasprimeCpuValue> rasprimeCpuValues,
        @Event final Observable<PicameraACpuValue> picameraACpuValues,
        @Event final Observable<PicameraBCpuValue> picameraBCpuValues,
        @Event final Observable<InternalPressureValue> internalPressure,
        @Event final Observable<ExternalPressureValue> externalPressureA,
        @Event final Observable<InternalTemperatureValue> internalTemperature
    ) {
        this.view = view;
        this.rasprimeCpuInformationView = rasprimeCpuInformationView;
        this.picameraACpuInformationView = picameraACpuInformationView;
        this.picameraBCpuInformationView = picameraBCpuInformationView;
        this.rasprimeCpuValues = rasprimeCpuValues;
        this.picameraACpuValues = picameraACpuValues;
        this.picameraBCpuValues = picameraBCpuValues;
        this.subscriptions = new CompositeSubscription();

        this.internalPressure = internalPressure;
        this.externalPressureA = externalPressureA;
        this.internalTemperature = internalTemperature;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().addAll(
            rasprimeCpuInformationView.getParent(),
            picameraACpuInformationView.getParent(),
            picameraBCpuInformationView.getParent());

        subscriptions.add(rasprimeCpuValues.observeOn(JAVA_FX_SCHEDULER)
            .subscribe(v -> updateCpuValueLabels(rasprimeCpuInformationView, v)));
        subscriptions.add(picameraACpuValues.observeOn(JAVA_FX_SCHEDULER)
            .subscribe(v -> updateCpuValueLabels(picameraACpuInformationView, v)));
        subscriptions.add(picameraBCpuValues.observeOn(JAVA_FX_SCHEDULER)
            .subscribe(v -> updateCpuValueLabels(picameraBCpuInformationView, v)));

        rasprimeCpuInformationView.name.setText("RasPrime");
        picameraACpuInformationView.name.setText("PiCameraA");
        picameraBCpuInformationView.name.setText("PiCameraB");

        subscriptions.add(internalPressure.observeOn(JAVA_FX_SCHEDULER).subscribe(this::updatePressureLabel));
        subscriptions.add(
            externalPressureA.map(ExternalPressureValue::getValue)
                .compose(new MedianTransformer<>(SENSOR_MEDIAN_SAMPLE_SIZE))
                .compose(new AverageTransformer<>(SENSOR_AVERAGE_SAMPLE_SIZE))
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

    private void updateCpuValueLabels(final CpuInformationView cpuView, final CpuValue value) {
        cpuView.temperatureLabel.setText(String.format("%.1f °C", value.getTemperature()));
        cpuView.voltageLabel.setText(String.format("%.2f V", value.getVoltage()));
    }
}
