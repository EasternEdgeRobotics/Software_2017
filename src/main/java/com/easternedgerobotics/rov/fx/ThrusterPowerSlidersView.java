package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class ThrusterPowerSlidersView implements View {
    static final int SPACING = 10;

    final HBox row = new HBox(SPACING);

    final SliderView globalSliderView = new SliderView("Global", 0);

    final SliderView heaveSliderView = new SliderView("Heave", SliderView.MAX_VALUE);

    final SliderView swaySliderView = new SliderView("Sway", SliderView.MAX_VALUE);

    final SliderView surgeSliderView = new SliderView("Surge", SliderView.MAX_VALUE);

    final SliderView yawSliderView = new SliderView("Yaw", SliderView.MAX_VALUE);

    final SliderView rollSliderView = new SliderView("Roll", SliderView.MAX_VALUE);

    @Inject
    public ThrusterPowerSlidersView() {
        row.setPadding(new Insets(SPACING));
        row.getChildren().addAll(
            globalSliderView.getParent(),
            heaveSliderView.getParent(),
            swaySliderView.getParent(),
            surgeSliderView.getParent(),
            yawSliderView.getParent(),
            rollSliderView.getParent());
    }

    @Override
    public final Parent getParent() {
        return row;
    }
}
