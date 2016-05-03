package com.easternedgerobotics.rov.fx;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class SliderView implements View {
    static final String LABEL_FORMAT = "%.0f";

    static final double MAX_VALUE = 100;

    final Slider slider = new Slider(0, MAX_VALUE, MAX_VALUE);

    final Label valueLabel = new Label();

    final Label displayNameLabel = new Label();

    final VBox vbox;

    @Inject
    public SliderView() {
        final int tickInterval = 10;
        final int spacing = 10;

        slider.setOrientation(Orientation.VERTICAL);
        slider.setMajorTickUnit(tickInterval);
        slider.setMinorTickCount(0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        vbox = new VBox(spacing, slider, valueLabel, displayNameLabel);
    }

    @Override
    public final Parent getParent() {
        return vbox;
    }
}
