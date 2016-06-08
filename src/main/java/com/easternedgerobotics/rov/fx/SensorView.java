package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.inject.Inject;

public class SensorView implements View {
    private static final int LABEL_SPACING = 20;

    private static final int CELL_SPACING = 10;

    static final String PRESSURE_LABEL_FORMAT = "%.1f kPa";

    static final String TEMPERATURE_LABEL_FORMAT = "%.1f °C";

    final Label internalPressureLabel = new Label("???");

    final Label externalPressureLabelA = new Label("???");

    final Label externalPressureLabelB = new Label("???");

    final Label internalTemperatureLabel = new Label("???");

    final Label externalTemperatureLabel = new Label("???");

    final HBox row;

    private final BorderPane node = new BorderPane();

    @Inject
    public SensorView() {
        this.row = new HBox(
            LABEL_SPACING,
            column(bold(internalPressureLabel), new Label("Internal Pressure")),
            column(bold(externalPressureLabelA), new Label("External Pressure A")),
            column(bold(externalPressureLabelB), new Label("External Pressure B")),
            column(bold(internalTemperatureLabel), new Label("Internal Temperature")),
            column(bold(externalTemperatureLabel), new Label("External Temperature"))
        );

        node.setPadding(new Insets(CELL_SPACING));
        node.setCenter(row);
    }

    /**
     * Returns the parent node for the view.
     * @return the parent node for the view
     */
    @Override
    public final Parent getParent() {
        return node;
    }

    private VBox column(final Label... rows) {
        final VBox col = new VBox(LABEL_SPACING, rows);
        col.setAlignment(Pos.BASELINE_CENTER);

        return col;
    }

    private Label bold(final Label label) {
        final Font font = label.getFont();
        label.setFont(Font.font(font.getFamily(), FontWeight.BOLD, font.getSize()));

        return label;
    }
}
