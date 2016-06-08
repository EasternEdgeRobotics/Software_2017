package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public final class EnableView implements View {
    final CheckBox enable = new CheckBox();

    final Label displayNameLabel = new Label();

    final VBox vbox;

    @Inject
    public EnableView() {
        final int spacing = 10;
        enable.setSelected(true);
        vbox = new VBox(spacing, enable, displayNameLabel);
    }

    @Override
    public Parent getParent() {
        return vbox;
    }
}
