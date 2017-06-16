package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.fx.distance.AxisNode;
import com.easternedgerobotics.rov.fx.distance.TextNode;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

    final Button calculateButton = new Button("Calculate");

    final TextField xAxisLength = new TextField();

    final ImageView imageView = new ImageView();

    final StackPane imageStack = new StackPane();

    final AtomicReference<AxisNode> axisNode = new AtomicReference<>(new AxisNode());

    final List<TextNode> imagePoints = new ArrayList<>();

    @Inject
    public DistanceCalculatorView() {
        final List<Button> buttons = Arrays.asList(captureA, captureB);
        for (final Button button : buttons) {
            button.setPrefHeight(BUTTON_HEIGHT);
            button.prefWidthProperty().bind(buttonPanel.widthProperty().divide(buttons.size()));
        }
        buttonPanel.getChildren().addAll(buttons);
        calculateButton.setPrefHeight(BUTTON_HEIGHT);
        calculateButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        xAxisLength.setPrefHeight(BUTTON_HEIGHT);
        xAxisLength.prefWidthProperty().bind(buttonPanel.widthProperty());
        xAxisLength.setPromptText("X Axis Length");

        galleryBorderPane.setBorder(new Border(new BorderStroke(Color.GRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(RADIUS), BorderWidths.DEFAULT)));

        galleryBorderPane.prefHeightProperty().bind(imageSelectionPanel.heightProperty().subtract(BUTTON_HEIGHT * 2));

        imageSelectionPanel.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        imageSelectionPanel.setSpacing(PADDING);
        imageSelectionPanel.getChildren().addAll(buttonPanel, xAxisLength, calculateButton, galleryBorderPane);
        imageSelectionPanel.setMaxWidth(SELECTION_WIDTH);
        imageSelectionPanel.setMinWidth(SELECTION_WIDTH);

        imageView.setStyle("-fx-background-color: BLACK");
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        calculatorBorderPane.setCenter(imageStack);
        calculatorBorderPane.setStyle("-fx-background-color: BLACK");
        calculatorBorderPane.setBorder(new Border(new BorderStroke(Color.GRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(RADIUS), BorderWidths.DEFAULT)));
        calculatorBorderPane.prefWidthProperty().bind(mainPanel.widthProperty().subtract(SELECTION_WIDTH));
        calculatorBorderPane.prefHeightProperty().bind(mainPanel.heightProperty());

        mainPanel.getChildren().addAll(calculatorBorderPane, imageSelectionPanel);

        borderPane.setCenter(mainPanel);
    }

    public void setImage(final Image image) {
        imageView.setFitWidth(borderPane.getWidth() - SELECTION_WIDTH);
        imageView.setFitHeight(borderPane.getHeight());
        imageView.setImage(image);
    }

    @Override
    public Parent getParent() {
        return borderPane;
    }
}
