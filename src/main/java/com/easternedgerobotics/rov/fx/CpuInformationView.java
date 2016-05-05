package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class CpuInformationView implements View {
    final Label frequencyLabel = new Label();

    final Label temperatureLabel = new Label();

    final Label voltageLabel = new Label();

    final BorderPane borderPane;

    @Inject
    public CpuInformationView() {
        final int labelSpacing = 5;

        final VBox column = new VBox(
            labelSpacing,
            new HBox(labelSpacing, new Label("Voltage: "), voltageLabel),
            new HBox(labelSpacing, new Label("CPU temperature: "), temperatureLabel),
            new HBox(labelSpacing, new Label("CPU frequency: "), frequencyLabel)
        );

        column.setPadding(new Insets(labelSpacing));

        borderPane = new BorderPane(column);
    }

    @Override
    public final Parent getParent() {
        return borderPane;
    }
}
