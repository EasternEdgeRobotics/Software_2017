package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoystickView implements View {
    static final int BUTTON_COUNT = 12;

    static final int SLIDER_MAX = 1;

    static final int PADDING = 2;

    static final int SPACING = 5;

    final Slider yAxis = new Slider(0, SLIDER_MAX, 0);

    final Slider xAxis = new Slider(0, SLIDER_MAX, 0);

    final Slider zAxis = new Slider(0, SLIDER_MAX, 0);

    final List<ToggleButton> buttons;

    final BorderPane layout;

    public JoystickView() {
        buttons = IntStream.range(1, BUTTON_COUNT + 1)
            .mapToObj(this::buttonForIdx)
            .collect(Collectors.toList());

        xAxis.setShowTickMarks(true);
        yAxis.setShowTickMarks(true);
        zAxis.setShowTickMarks(true);

        xAxis.setDisable(true);
        yAxis.setDisable(true);
        zAxis.setDisable(true);

        xAxis.setOrientation(Orientation.VERTICAL);
        yAxis.setOrientation(Orientation.VERTICAL);
        zAxis.setOrientation(Orientation.VERTICAL);

        buttons.forEach(button -> {
            button.setDisable(true);
            button.setMaxWidth(Double.MAX_VALUE);
        });

        final HBox center = new HBox(SPACING, xAxis, yAxis, zAxis);
        center.setPadding(new Insets(PADDING));

        final TilePane tilePane = new TilePane(SPACING, SPACING, buttons.toArray(new ToggleButton[buttons.size()]));
        tilePane.setPadding(new Insets(1));

        layout = new BorderPane(center);
        layout.setRight(tilePane);
    }

    @Override
    public final Parent getParent() {
        return layout;
    }

    private ToggleButton buttonForIdx(final int index) {
        return new ToggleButton(Integer.toString(index));
    }
}
