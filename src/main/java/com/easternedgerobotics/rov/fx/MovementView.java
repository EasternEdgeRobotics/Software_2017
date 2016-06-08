package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public final class MovementView implements View {
    final VBox vbox;

    @Inject
    public MovementView() {
        final int spacing = 10;
        vbox = new VBox(spacing);
    }

    @Override
    public Parent getParent() {
        return vbox;
    }
}
