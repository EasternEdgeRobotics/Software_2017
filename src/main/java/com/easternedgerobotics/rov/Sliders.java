package com.easternedgerobotics.rov;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public final class Sliders {

    private Sliders() {

    }

    public static void display(final String secondaryStage) {
        final Stage window = new Stage();
        window.setTitle(secondaryStage);
        Slider pitch;
        Slider yaw;
        Slider roll;
        Slider surge;
        Slider heave;
        Slider sway;
        final int max = 100;
        final int start = 100;
        pitch = new Slider(0, max, start);
        yaw = new Slider(0, max, start);
        roll = new Slider(0, max, start);
        surge = new Slider(0, max, start);
        heave = new Slider(0, max, start);
        sway = new Slider(0, max, start);
        final int tickUnit = 20;
        pitch.setOrientation(Orientation.VERTICAL);
        pitch.setMajorTickUnit(tickUnit);
        pitch.setShowTickMarks(true);
        pitch.setShowTickLabels(true);
        yaw.setOrientation(Orientation.VERTICAL);
        yaw.setMajorTickUnit(tickUnit);
        yaw.setShowTickMarks(true);
        yaw.setShowTickLabels(true);
        roll.setOrientation(Orientation.VERTICAL);
        roll.setMajorTickUnit(tickUnit);
        roll.setShowTickMarks(true);
        roll.setShowTickLabels(true);
        surge.setOrientation(Orientation.VERTICAL);
        surge.setMajorTickUnit(tickUnit);
        surge.setShowTickMarks(true);
        surge.setShowTickLabels(true);
        heave.setOrientation(Orientation.VERTICAL);
        heave.setMajorTickUnit(tickUnit);
        heave.setShowTickMarks(true);
        heave.setShowTickLabels(true);
        sway.setOrientation(Orientation.VERTICAL);
        sway.setMajorTickUnit(tickUnit);
        sway.setShowTickMarks(true);
        sway.setShowTickLabels(true);

        final Label pitchLabel = new Label("Pitch");
        final Label yawLabel = new Label("Yaw");
        final Label rollLabel = new Label("Roll");
        final Label surgeLabel = new Label("Surge");
        final Label heaveLabel = new Label("Heave");
        final Label swayLabel = new Label("Sway");

        final Label pitchValue = new Label(
            Double.toString(pitch.getValue()));
        final Label yawValue = new Label(
            Double.toString(yaw.getValue()));
        final Label rollValue = new Label(
            Double.toString(roll.getValue()));
        final Label surgeValue = new Label(
            Double.toString(surge.getValue()));
        final Label heaveValue = new Label(
            Double.toString(heave.getValue()));
        final Label swayValue = new Label(
            Double.toString(sway.getValue()));

        pitch.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val1 = newVal.doubleValue();
                pitchValue.setText(String.format("%.1f", val1));
            }
        });
        yaw.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val2 = newVal.doubleValue();
                yawValue.setText(String.format("%.1f", val2));
            }
        });
        roll.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val3 = newVal.doubleValue();
                rollValue.setText(String.format("%.1f", val3));
            }
        });
        surge.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val4 = newVal.doubleValue();
                surgeValue.setText(String.format("%.1f", val4));
            }
        });
        heave.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val5 = newVal.doubleValue();
                heaveValue.setText(String.format("%.1f", val5));
            }
        });
        sway.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(final ObservableValue<? extends Number> ov,
                                final Number oldVal, final Number newVal) {
                final double val6 = newVal.doubleValue();
                swayValue.setText(String.format("%.1f", val6));
            }
        });

        final int insetsCenter1 = 8;
        final int insetsCenter2 = 15;
        final int insetsTop1 = 2;
        final int insetsTop2 = 7;
        final int insetsBottom = 5;
        final int spacing1 = 15;
        final int spacing2 = 20;
        final HBox center = new HBox();
        center.setPadding(new Insets(insetsCenter1, insetsCenter1, insetsCenter1, insetsCenter2));
        center.setSpacing(spacing1);
        center.getChildren().addAll(pitch, yaw, roll, surge, heave, sway);

        final HBox top = new HBox();
        top.setPadding(new Insets(insetsTop1, insetsTop1, insetsTop1, insetsTop2));
        top.setSpacing(spacing2);
        top.getChildren().addAll(pitchLabel, yawLabel, rollLabel, surgeLabel, heaveLabel, swayLabel);

        final HBox bottom = new HBox();
        bottom.setPadding(new Insets(insetsBottom, insetsBottom, insetsBottom, insetsBottom));
        bottom.setSpacing(spacing1);
        bottom.getChildren().addAll(pitchValue, yawValue, rollValue, surgeValue, heaveValue, swayValue);

        final BorderPane control = new BorderPane();
        control.setCenter(center);
        control.setTop(top);
        control.setBottom(bottom);

        final int minHeight = 200;
        final int minWidth = 300;
        final int width = 300;
        final int height = 200;
        final Scene box = new Scene(control, width, height);
        window.setMinHeight(minHeight);
        window.setMinWidth(minWidth);
        window.setScene(box);
        window.show();
    }
}

