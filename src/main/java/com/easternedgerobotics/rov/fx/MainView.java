package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

public class MainView implements View {
    static final int SPACING = 10;

    final BorderPane box = new BorderPane();

    final ToggleButton button = new ToggleButton("Start");

    @Inject
    public MainView() {
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        box.setPadding(new Insets(SPACING));
        box.setCenter(button);
    }

    @Override
    public final Parent getParent() {
        return box;
    }
}
