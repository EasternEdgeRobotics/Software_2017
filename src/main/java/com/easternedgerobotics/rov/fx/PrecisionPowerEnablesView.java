package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class PrecisionPowerEnablesView implements View {
    static final String POWER_LABEL_FORMAT = "%d%%";

    static final int SPACING = 10;

    final HBox row = new HBox(SPACING);

    final Label powerLabel = new Label("???");

    @Inject
    public PrecisionPowerEnablesView() {
        row.setPadding(new Insets(SPACING));
        row.getChildren().add(new Label("Precision\nPower"));
        row.getChildren().add(powerLabel);
    }

    @Override
    public final Parent getParent() {
        return row;
    }
}
