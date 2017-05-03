package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class MainView implements View {
    static final int SPACING = 10;

    static final int BOX_W = 256;

    static final int BOX_H = 32;

    final BorderPane box = new BorderPane();

    final VBox buttonPanel = new VBox();

    static final int START_BUTTON_WEIGHT = 3;

    final ToggleButton startButton = new ToggleButton("Start");

    static final int THRUSTER_VIEW_WEIGHT = 1;

    final Button thrusterButton = new Button("Thruster View");

    static final int SENSOR_VIEW_WEIGHT = 1;

    final Button sensorButton = new Button("Sensor View");

    static final int CAMERA_VIEW_WEIGHT = 1;

    final Button cameraButton = new Button("Camera View");

    static final int RESET_CAMERA_WEIGHT = 1;

    final Button resetCameraButton = new Button("Reset Camera Feed");

    static final int TOTAL_WEIGHT =
        START_BUTTON_WEIGHT
        + THRUSTER_VIEW_WEIGHT
        + SENSOR_VIEW_WEIGHT
        + CAMERA_VIEW_WEIGHT
        + RESET_CAMERA_WEIGHT;

    @Inject
    public MainView() {
        startButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        thrusterButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        sensorButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        cameraButton.prefWidthProperty().bind(buttonPanel.widthProperty());
        resetCameraButton.prefWidthProperty().bind(buttonPanel.widthProperty());

        startButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(START_BUTTON_WEIGHT).divide(TOTAL_WEIGHT));
        thrusterButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(THRUSTER_VIEW_WEIGHT).divide(TOTAL_WEIGHT));
        sensorButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(SENSOR_VIEW_WEIGHT).divide(TOTAL_WEIGHT));
        cameraButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(CAMERA_VIEW_WEIGHT).divide(TOTAL_WEIGHT));
        resetCameraButton.prefHeightProperty().bind(buttonPanel.heightProperty()
            .multiply(RESET_CAMERA_WEIGHT).divide(TOTAL_WEIGHT));

        buttonPanel.getChildren().addAll(startButton, thrusterButton, sensorButton, cameraButton, resetCameraButton);
        buttonPanel.setPrefWidth(BOX_W);
        buttonPanel.setPrefHeight(BOX_H * TOTAL_WEIGHT);
        box.setPadding(new Insets(SPACING));
        box.setCenter(buttonPanel);
    }

    @Override
    public final Parent getParent() {
        return box;
    }
}
