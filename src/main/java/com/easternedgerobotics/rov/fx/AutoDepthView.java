package com.easternedgerobotics.rov.fx;

import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class AutoDepthView implements View {
    final BorderPane border = new BorderPane();

    final HBox hbox = new HBox();

    final TextField text = new TextField();

    final ToggleButton button = new ToggleButton("Enable");

    @Inject
    public AutoDepthView() {
        hbox.getChildren().addAll(text, button);
        text.prefHeightProperty().bind(border.heightProperty());
        button.prefHeightProperty().bind(border.heightProperty());
        text.prefWidthProperty().bind(border.widthProperty().divide(2));
        button.prefWidthProperty().bind(border.widthProperty().divide(2));
        border.setCenter(hbox);
    }

    @Override
    public Parent getParent() {
        return border;
    }
}
