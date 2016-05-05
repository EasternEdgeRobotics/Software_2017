package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class SensorView implements View {
    private static final int SPACING = 5;

    final HBox row;

    @Inject
    public SensorView() {
        this.row = new HBox(SPACING);
    }

    /**
     * Returns the parent node for the view.
     * @return the parent node for the view
     */
    @Override
    public final Parent getParent() {
        return row;
    }
}
