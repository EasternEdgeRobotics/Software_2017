package com.easternedgerobotics.rov.fx;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class SliderView implements View {
    static final String LABEL_FORMAT = "%.0f";

    static final double MAX_VALUE = 100;

    final Slider slider = new Slider(0, MAX_VALUE, 0);

    final Label valueLabel = new Label();

    final VBox vbox;

    /**
     * Constructs a @{code SliderView} with the given name and value.
     * @param name the text of slider label
     * @param value the default value for the slider
     */
    SliderView(final String name, final double value) {
        final Label displayNameLabel = new Label(name);
        final int spacing = 10;
        final int tickInterval = 10;

        slider.setValue(value);
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
