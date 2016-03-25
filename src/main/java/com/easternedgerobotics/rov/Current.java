package com.easternedgerobotics.rov;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class Current {

    private Current() {

    }

    public static void display(final String primaryStage) {
        final Stage botLeft  = new Stage();
        botLeft.setTitle(primaryStage);
        final Label tempIntC = new Label("Internal Temperature: ");
        final Label current1 = new Label("Current 1: ");
        final Label current2 = new Label("Current 2: ");
        final Label current3 = new Label("Current 3: ");
        final Label current4 = new Label("Current 4: ");
        final Label valueIntC = new Label("25");
        final Label value1 = new Label("5");
        final Label value2 = new Label("6");
        final Label value3 = new Label("5");
        final Label value4 = new Label("6");
        final Label tempUnitC = new Label("°C");
        final Label currentUnit1 = new Label("A");
        final Label currentUnit2 = new Label("A");
        final Label currentUnit3 = new Label("A");
        final Label currentUnit4 = new Label("A");

        final int padding = 2;
        final int spacing = 5;
        final VBox name = new VBox();
        name.setPadding(new Insets(padding, padding, padding, padding));
        name.setSpacing(spacing);
        name.getChildren().addAll(tempIntC, current1, current2, current3, current4);

        final VBox number = new VBox();
        number.setPadding(new Insets(padding, padding, padding, padding));
        number.setSpacing(spacing);
        number.getChildren().addAll(valueIntC, value1, value2, value3, value4);

        final VBox unit = new VBox();
        unit.setPadding(new Insets(padding, padding, padding, padding));
        unit.setSpacing(spacing);
        unit.getChildren().addAll(tempUnitC, currentUnit1, currentUnit2, currentUnit3, currentUnit4);

        final BorderPane layout = new BorderPane();
        layout.setCenter(number);
        layout.setLeft(name);
        layout.setRight(unit);

        final int width = 200;
        final int height = 150;
        final int minWidth = 200;
        final int minHeight = 150;
        final Scene current = new Scene(layout, width, height);
        botLeft.setMinHeight(minHeight);
        botLeft.setMinWidth(minWidth);
        botLeft.setScene(current);
        botLeft.show();
    }
}
