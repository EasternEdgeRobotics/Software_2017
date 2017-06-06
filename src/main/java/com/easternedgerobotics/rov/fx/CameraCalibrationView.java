package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

public final class CameraCalibrationView implements View {
    static final double PADDING = 10;

    static final int BUTTON_HEIGHT = 40;

    static final double RADIUS = 3;

    final TabPane tabPane = new TabPane();

    final BorderPane tabBorderPane = new BorderPane();

    final HBox buttonPanel = new HBox();

    final VBox mainPanel = new VBox();

    final BorderPane borderPane = new BorderPane();

    final Button captureCalibrateA = new Button("Capture A (Calibrate)");

    final Button captureCalibrateB = new Button("Capture B (Calibrate)");

    final Button calibrate = new Button("Calibrate");

    final Tab cameraACalibrationTab = new Tab();

    final Tab cameraBCalibrationTab = new Tab();

    @Inject
    public CameraCalibrationView() {
        final List<Button> buttons = Arrays.asList(captureCalibrateA, captureCalibrateB, calibrate);
        for (final Button button : buttons) {
            button.setPrefHeight(BUTTON_HEIGHT);
            button.prefWidthProperty().bind(buttonPanel.widthProperty().divide(buttons.size()));
        }
        buttonPanel.getChildren().addAll(buttons);

        tabPane.setBorder(new Border(new BorderStroke(Color.GRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(RADIUS), BorderWidths.DEFAULT)));

        tabBorderPane.setPadding(new Insets(PADDING, 0, PADDING, 0));
        tabBorderPane.setCenter(tabPane);
        tabBorderPane.prefHeightProperty().bind(mainPanel.heightProperty().subtract(BUTTON_HEIGHT));

        cameraACalibrationTab.setText("Camera A Calibration");
        cameraACalibrationTab.setClosable(false);

        cameraBCalibrationTab.setText("Camera B Calibration");
        cameraBCalibrationTab.setClosable(false);

        tabPane.getTabs().addAll(cameraACalibrationTab, cameraBCalibrationTab);

        mainPanel.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        mainPanel.getChildren().addAll(buttonPanel, tabBorderPane);

        borderPane.setCenter(mainPanel);
    }

    @Override
    public Parent getParent() {
        return borderPane;
    }
}
