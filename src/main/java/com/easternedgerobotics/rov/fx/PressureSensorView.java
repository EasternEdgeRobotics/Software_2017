package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PressureSensorView implements View {
    final Label internalPressureLabel = new Label();

    final BorderPane borderPane;

    public PressureSensorView() {
        final int labelSpacing = 5;

        final VBox column = new VBox(
            labelSpacing,
            new HBox(labelSpacing, new Label("Internal pressure: "), internalPressureLabel)
        );

        column.setPadding(new Insets(labelSpacing));

        borderPane = new BorderPane(column);
    }

    @Override
    public final Parent getParent() {
        return borderPane;
    }
}
