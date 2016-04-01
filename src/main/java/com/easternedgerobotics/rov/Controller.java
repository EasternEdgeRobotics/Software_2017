package com.easternedgerobotics.rov;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public final class Controller {

    private Controller() {

    }

    public static void display(final String primaryStage) {
        final Stage window = new Stage();
        window.setTitle(primaryStage);
        final int sliderMax = 100;
        final int sliderInitial = 49;
        final Slider xAxis = new Slider();
        xAxis.setMin(0);
        xAxis.setMax(sliderMax);
        xAxis.setValue(sliderInitial);
        xAxis.setOrientation(Orientation.VERTICAL);
        final Slider yAxis = new Slider(0, sliderMax, sliderInitial);
        yAxis.setOrientation(Orientation.VERTICAL);
        final Slider zAxis = new Slider(0, sliderMax, sliderInitial);
        zAxis.setOrientation(Orientation.VERTICAL);
        final Button but0 = new Button("1");
        final Button but1 = new Button("2");
        final Button but2 = new Button("3");
        final Button but3 = new Button("4");
        final Button but4 = new Button("5");
        final Button but5 = new Button("6");
        final Button but6 = new Button("7");
        final Button but7 = new Button("8");
        final Button but8 = new Button("9");
        final Button but9 = new Button("10");
        final Button but10 = new Button("11");
        final Button but11 = new Button("12");
        but0.setMaxWidth(Double.MAX_VALUE);
        but1.setMaxWidth(Double.MAX_VALUE);
        but2.setMaxWidth(Double.MAX_VALUE);
        but3.setMaxWidth(Double.MAX_VALUE);
        but4.setMaxWidth(Double.MAX_VALUE);
        but5.setMaxWidth(Double.MAX_VALUE);
        but6.setMaxWidth(Double.MAX_VALUE);
        but7.setMaxWidth(Double.MAX_VALUE);
        but8.setMaxWidth(Double.MAX_VALUE);
        but9.setMaxWidth(Double.MAX_VALUE);
        but10.setMaxWidth(Double.MAX_VALUE);
        but11.setMaxWidth(Double.MAX_VALUE);

        but0.setDisable(true);
        but1.setDisable(true);
        but2.setDisable(true);
        but3.setDisable(true);
        but4.setDisable(true);
        but5.setDisable(true);
        but6.setDisable(true);
        but7.setDisable(true);
        but8.setDisable(true);
        but9.setDisable(true);
        but10.setDisable(true);
        but11.setDisable(true);
        xAxis.setDisable(true);
        yAxis.setDisable(true);
        zAxis.setDisable(true);

        final int padding = 2;
        final int spacing = 5;
        final int gap = 4;
        final HBox center = new HBox();
        center.setPadding(new Insets(padding, padding, padding, padding));
        center.setSpacing(spacing);
        center.getChildren().addAll(xAxis, yAxis, zAxis);

        final TilePane right = new TilePane();
        right.setPadding(new Insets(1, 1, 1, 1));
        right.setVgap(gap);
        right.setHgap(gap);
        right.setPrefColumns(2);
        right.getChildren().addAll(but0, but1, but2, but3, but4, but5, but6, but7, but8, but9, but10, but11);

        final BorderPane layout = new BorderPane();
        layout.setCenter(center);
        layout.setRight(right);

        final Scene scene = new Scene(layout, 200, 150);
        window.setScene(scene);
        window.show();
    }
}

