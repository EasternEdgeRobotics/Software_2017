package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class CpuInformationView implements View {
    private static final int LABEL_SPACING = 5;

    final Label frequencyLabel = new Label("???");

    final Label temperatureLabel = new Label("???");

    final Label voltageLabel = new Label("???");

    final HBox row;

    @Inject
    public CpuInformationView() {
        row = new HBox(
            LABEL_SPACING,
            new VBox(LABEL_SPACING, voltageLabel, new Label("Voltage")),
            new VBox(LABEL_SPACING, temperatureLabel, new Label("CPU temperature")),
            new VBox(LABEL_SPACING, frequencyLabel, new Label("CPU frequency"))
        );
    }

    @Override
    public final Parent getParent() {
        return row;
    }
}
