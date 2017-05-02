package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public final class VideoView implements View {
    static final int SPACING = 0;

    static final int WIDTH = 1296;

    static final int HEIGHT = 720;

    final HBox row = new HBox(SPACING);

    final ImageView cameraA = new ImageView();

    final ImageView cameraB = new ImageView();

    @Inject
    public VideoView() {
        row.setPadding(new Insets(SPACING));
        row.getChildren().addAll(
            cameraA, cameraB);
        cameraA.fitHeightProperty().bind(row.widthProperty().divide(2).divide(WIDTH).multiply(HEIGHT));
        cameraB.fitHeightProperty().bind(row.widthProperty().divide(2).divide(WIDTH).multiply(HEIGHT));
        cameraA.fitWidthProperty().bind(row.widthProperty().divide(2));
        cameraB.fitWidthProperty().bind(row.widthProperty().divide(2));
    }

    @Override
    public Parent getParent() {
        return row;
    }
}
