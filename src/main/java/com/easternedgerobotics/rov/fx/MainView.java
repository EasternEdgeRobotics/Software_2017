package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class MainView implements View {
    static final int SPACING = 10;

    static final int BOX_W = 256;

    static final int BOX_H = 32;

    final BorderPane box = new BorderPane();

    static final int HEARTBEAT_WEIGHT = 1;

    static final Background FOUND_BG = new Background(new BackgroundFill(
        Color.GREEN, new CornerRadii(3), new Insets(0)));

    static final Background LOST_BG = new Background(new BackgroundFill(
        Color.RED, new CornerRadii(3), new Insets(0)));

    final HBox heartbeatPanel = new HBox();

    final ToggleButton rasprimeIndicator = new ToggleButton("RasPrime");

    final ToggleButton picameraAIndicator = new ToggleButton("PiCameraA");

    final ToggleButton picameraBIndicator = new ToggleButton("PiCameraB");

    final VBox buttonPanel = new VBox();

    static final int START_BUTTON_WEIGHT = 3;

    final ToggleButton startButton = new ToggleButton("Start");

    static final int NORMAL_BUTTON_WEIGHT = 1;

    final Button thrusterButton = new Button("Thruster View");

    final Button sensorButton = new Button("Sensor View");

    final Button cameraButton = new Button("Camera View");

    final Button resetCameraButton = new Button("Reset Camera Feed");

    final Button calibrationButton = new Button("Camera Calibration Images");

    final Button distanceButton = new Button("Distance Calculator");

    @Inject
    public MainView() {
        final List<ToggleButton> indicatorButtons = Arrays.asList(
            rasprimeIndicator,
            picameraAIndicator,
            picameraBIndicator);

        final List<Button> normalButtons = Arrays.asList(
            thrusterButton,
            sensorButton,
            cameraButton,
            resetCameraButton,
            calibrationButton,
            distanceButton);

        final int totalWeight = START_BUTTON_WEIGHT + HEARTBEAT_WEIGHT + (normalButtons.size() * NORMAL_BUTTON_WEIGHT);

        startButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        startButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(START_BUTTON_WEIGHT).divide(totalWeight));
        buttonPanel.getChildren().addAll(startButton);

        normalButtons.forEach(button -> {
            button.prefWidthProperty().bind(buttonPanel.widthProperty());
            button.prefHeightProperty().bind(buttonPanel.prefHeightProperty()
                .multiply(NORMAL_BUTTON_WEIGHT).divide(totalWeight));
        });
        buttonPanel.getChildren().addAll(normalButtons);

        indicatorButtons.forEach(button -> {
            button.setDisable(true);
            button.prefWidthProperty().bind(heartbeatPanel.widthProperty().divide(indicatorButtons.size()));
            button.prefHeightProperty().bind(heartbeatPanel.prefHeightProperty());
            button.setFont(Font.font(button.getFont().getName(), FontWeight.BOLD, button.getFont().getSize()));
            button.setTextFill(Color.BLACK);
            button.setOpacity(1);
            button.setBackground(LOST_BG);
        });
        heartbeatPanel.getChildren().addAll(indicatorButtons);
        heartbeatPanel.prefWidthProperty().bind(buttonPanel.widthProperty());
        heartbeatPanel.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(HEARTBEAT_WEIGHT).divide(totalWeight));
        buttonPanel.getChildren().addAll(heartbeatPanel);

        buttonPanel.prefWidthProperty().bind(box.widthProperty());
        buttonPanel.prefHeightProperty().bind(box.heightProperty());
        box.setPrefWidth(BOX_W);
        box.setPrefHeight(BOX_H * totalWeight);
        box.setPadding(new Insets(SPACING));
        box.setCenter(buttonPanel);
    }

    @Override
    public final Parent getParent() {
        return box;
    }
}
