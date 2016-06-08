package com.easternedgerobotics.rov.fx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.inject.Inject;

public class CpuInformationView implements View {
    private static final int LABEL_SPACING = 20;

    final Label frequencyLabel = new Label("???");

    final Label temperatureLabel = new Label("???");

    final Label voltageLabel = new Label("???");

    final Label name = new Label("Rasprime");

    final VBox row;

    @Inject
    public CpuInformationView() {
        this.row = column(
            new HBox(
                LABEL_SPACING,
                column(bold(voltageLabel), new Label("Voltage")),
                column(bold(temperatureLabel), new Label("CPU temperature")),
                column(bold(frequencyLabel), new Label("CPU frequency"))
            ),
            name
        );
    }

    @Override
    public final Parent getParent() {
        return row;
    }

    private Label bold(final Label label) {
        final Font font = label.getFont();
        label.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));

        return label;
    }

    private VBox column(final Node... rows) {
        final VBox col = new VBox(LABEL_SPACING, rows);
        col.setAlignment(Pos.BASELINE_CENTER);

        return col;
    }
}
