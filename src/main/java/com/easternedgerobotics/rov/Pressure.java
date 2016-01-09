package com.easternedgerobotics.rov;

import java.util.prefs.Preferences;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class Pressure {

    private Pressure() {

    }

    public static void display(final String primaryStage) {
        final Stage botRight = new Stage();
        botRight.setTitle(primaryStage);
        final Label tempExtC = new Label("External Temperature: ");
        final Label tempExtF = new Label("External Tempertaure: ");
        final Label pressureInt = new Label("Internal Pressure: ");
        final Label pressureExt1 = new Label("Depth 1: ");
        final Label pressureExt2 = new Label("Depth 2: ");
        final Label pressureAvg = new Label("Average Depth: ");
        final Label pressureAvgL = new Label("Locked Depth: ");
        final Label valueExtC = new Label("5");
        final Label valueExtF = new Label("6");
        final Label valueInt = new Label("6");
        final Label valueExt1 = new Label("5");
        final Label valueExt2 = new Label("6");
        final Label valueAvg = new Label("5");
        final Label valueAvgL = new Label("6");
        final Label tempUnitC = new Label("°C");
        final Label tempUnitF = new Label("°F");
        final Label pressureUnitInt = new Label("kPa");
        final Label pressureUnitExt1 = new Label("m");
        final Label pressureUnitExt2 = new Label("m");
        final Label pressureUnitAvg = new Label("m");
        final Label pressureUnitAvgL = new Label("m");
        final Button lock = new Button("Lock");

        final int padding = 2;
        final int spacing = 5;
        final VBox name = new VBox();
        name.setPadding(new Insets(padding, padding, padding, padding));
        name.setSpacing(spacing);
        name.getChildren().addAll(tempExtC, tempExtF,  pressureInt, pressureExt1,
            pressureExt2, pressureAvg, pressureAvgL);

        final VBox number = new VBox();
        number.setPadding(new Insets(padding, padding, padding, padding));
        number.setSpacing(spacing);
        number.getChildren().addAll(valueExtC, valueExtF, valueInt, valueExt1,
            valueExt2, valueAvg, valueAvgL);

        final VBox unit = new VBox();
        unit.setPadding(new Insets(padding, padding, padding, padding));
        unit.setSpacing(spacing);
        unit.getChildren().addAll(tempUnitC, tempUnitF, pressureUnitInt,
            pressureUnitExt1, pressureUnitExt2, pressureUnitAvg, pressureUnitAvgL);

        final BorderPane initial = new BorderPane();
        initial.setCenter(number);
        initial.setLeft(name);
        initial.setRight(unit);

        final int paddingTop = 120;
        final VBox button = new VBox();
        button.setPadding(new Insets(paddingTop, padding, padding, padding));
        button.getChildren().addAll(lock);

        final BorderPane layout = new BorderPane();
        layout.setCenter(initial);
        layout.setLeft(button);

        final int height = 200;
        final int width = 300;
        final int minWidth = 300;
        final int minHeight = 200;
        final Scene pressure = new Scene(layout, width, height);
        botRight.setMinHeight(minHeight);
        botRight.setMinWidth(minWidth);
        botRight.setScene(pressure);
        botRight.show();
    }
}
