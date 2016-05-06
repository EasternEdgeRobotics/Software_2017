package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class InternalTemperatureSensorView implements View {
    private static final int LABEL_SPACING = 5;

    final Label sensorValueLabel = new Label("???");

    final VBox column;

    @Inject
    public InternalTemperatureSensorView() {
        column = new VBox(LABEL_SPACING, sensorValueLabel, new Label("Internal temperature"));
    }

    @Override
    public final Parent getParent() {
        return column;
    }
}
