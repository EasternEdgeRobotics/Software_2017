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
     * The CPU information sub-view.
     */
    private final CpuInformationView cpuInformationView;

    @Inject
    public SensorViewController(
        final SensorView view,
        final InternalTemperatureSensorView internalTemperatureSensorView,
        final CpuInformationView cpuInformationView
    ) {
        this.view = view;
        this.internalTemperatureSensorView = internalTemperatureSensorView;
        this.cpuInformationView = cpuInformationView;
    }

    @Override
    public final void onCreate() {
        view.row.getChildren().addAll(
            internalTemperatureSensorView.getParent(),
            cpuInformationView.getParent()
        );
    }
}
