package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    static final int SELECTION_WIDTH = 300;

    final BorderPane galleryBorderPane = new BorderPane();

    final BorderPane calculatorBorderPane = new BorderPane();

    final HBox mainPanel = new HBox();

    final HBox buttonPanel = new HBox();

    final VBox imageSelectionPanel = new VBox();

    final BorderPane borderPane = new BorderPane();

    final Button captureA = new Button("Capture A");

    final Button captureB = new Button("Capture B");

    final ImageView imageView = new ImageView();

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

        galleryBorderPane.prefHeightProperty().bind(imageSelectionPanel.heightProperty().subtract(BUTTON_HEIGHT));

        imageSelectionPanel.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        imageSelectionPanel.setSpacing(PADDING);
        imageSelectionPanel.getChildren().addAll(buttonPanel, galleryBorderPane);
        imageSelectionPanel.setMaxWidth(SELECTION_WIDTH);
        imageSelectionPanel.setMinWidth(SELECTION_WIDTH);

        calculatorBorderPane.setBorder(new Border(new BorderStroke(Color.GRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(RADIUS), BorderWidths.DEFAULT)));

        calculatorBorderPane.prefWidthProperty().bind(mainPanel.widthProperty().subtract(SELECTION_WIDTH));
        calculatorBorderPane.prefHeightProperty().bind(mainPanel.heightProperty());

        calculatorBorderPane.setCenter(imageView);
        calculatorBorderPane.setStyle("-fx-background-color: BLACK");
        imageView.setStyle("-fx-background-color: BLACK");
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        mainPanel.getChildren().addAll(calculatorBorderPane, imageSelectionPanel);

        borderPane.setCenter(mainPanel);
    }

    public void setImage(final Image image) {
        imageView.setImage(image);
        imageView.setFitWidth(borderPane.getWidth() - SELECTION_WIDTH);
        imageView.setFitHeight(borderPane.getHeight());
        imageView.setImage(imageView.getImage());
    }

    @Override
    public Parent getParent() {
        return borderPane;
    }
}
