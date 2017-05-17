package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public final class DistanceCalculatorView implements View {
    static final double PADDING = 10;

    static final int BUTTON_HEIGHT = 40;

    static final double RADIUS = 3;

    final BorderPane galleryBorderPane = new BorderPane();

    final HBox buttonPanel = new HBox();

    final VBox mainPanel = new VBox();

    final BorderPane borderPane = new BorderPane();

    final Button captureA = new Button("Capture A");

    final Button captureB = new Button("Capture B");

    @Inject
    public DistanceCalculatorView() {
        final List<Button> buttons = Arrays.asList(captureA, captureB);
        for (final Button button : buttons) {
            button.setPrefHeight(BUTTON_HEIGHT);
            button.prefWidthProperty().bind(buttonPanel.widthProperty().divide(buttons.size()));
        }
        buttonPanel.getChildren().addAll(buttons);

        galleryBorderPane.setBorder(new Border(new BorderStroke(Color.GRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(RADIUS), BorderWidths.DEFAULT)));

        galleryBorderPane.prefHeightProperty().bind(mainPanel.heightProperty().subtract(BUTTON_HEIGHT));

        mainPanel.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        mainPanel.setSpacing(PADDING);
        mainPanel.getChildren().addAll(buttonPanel, galleryBorderPane);

        borderPane.setCenter(mainPanel);
    }

    @Override
    public Parent getParent() {
        return borderPane;
    }
}
