package com.easternedgerobotics.rov.fx;

import javax.inject.Inject;

public class SensorViewController implements ViewController {
    /**
     * The sensor view.
     */
    private final SensorView view;

    /**
     * The internal temperature sensor sub-view.
     */
    private final InternalTemperatureSensorView internalTemperatureSensorView;

    /**
     * The internal pressure sensor sub-view.
     */
    private final InternalPressureSensorView internalPressureSensorView;

    /**
     * The CPU information sub-view.
     */
    private final CpuInformationView cpuInformationView;

    @Inject
    public SensorViewController(
        final SensorView view,
        final InternalTemperatureSensorView internalTemperatureSensorView,
        final InternalPressureSensorView internalPressureSensorView,
        final CpuInformationView cpuInformationView
    ) {
        this.view = view;
        this.internalTemperatureSensorView = internalTemperatureSensorView;
        this.internalPressureSensorView = internalPressureSensorView;
        this.cpuInformationView = cpuInformationView;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().addAll(
            internalTemperatureSensorView.getParent(),
            internalPressureSensorView.getParent(),
            cpuInformationView.getParent()
        );
    }
}
