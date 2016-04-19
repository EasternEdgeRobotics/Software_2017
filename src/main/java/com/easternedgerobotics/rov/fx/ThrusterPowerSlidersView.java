package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class ThrusterPowerSlidersView implements View {
    static final int SPACING = 10;

    final HBox row = new HBox(SPACING);

    @Inject
    public ThrusterPowerSlidersView() {
        row.setPadding(new Insets(SPACING));
    }

    @Override
    public final Parent getParent() {
        return row;
    }
}
