package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

class InternalPressureSensorView implements View {
    static final int LABEL_SPACING = 5;

    static final String PRESSURE_LABEL_FORMAT = "%.1f kPa";

    final Label sensorValueLabel = new Label("???");

    final VBox column;

    @Inject
    public InternalPressureSensorView() {
        column = new VBox(LABEL_SPACING, sensorValueLabel, new Label("Internal pressure"));
    }

    @Override
    public final Parent getParent() {
        return column;
    }
}
