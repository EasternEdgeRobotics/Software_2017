package com.easternedgerobotics.rov.fx;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import rx.subjects.PublishSubject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public final class GalleryView implements View {
    final PublishSubject<File> selected = PublishSubject.create();

    final PublishSubject<String> updateFolder = PublishSubject.create();

    static final double IMAGE_WIDTH = 150;

    static final double VBOX_PADDING = 5;

    static final double TILE_PADDING = 15;

    final List<ImageView> imageViews = new ArrayList<>();

    final ScrollPane scrollPane = new ScrollPane();

    final VBox vBox = new VBox();

    final Label folderLabel = new Label();

    final TilePane tilePane = new TilePane();

    @Inject
    public GalleryView() {
        scrollPane.setStyle("-fx-background-color: DAE6F3;");
        tilePane.setPadding(new Insets(TILE_PADDING, TILE_PADDING, TILE_PADDING, TILE_PADDING));
        tilePane.setHgap(TILE_PADDING);
        tilePane.setVgap(TILE_PADDING);

        vBox.setPadding(new Insets(VBOX_PADDING, VBOX_PADDING, VBOX_PADDING, VBOX_PADDING));
        vBox.getChildren().addAll(folderLabel, tilePane);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(vBox);
    }

    @Override
    public Parent getParent() {
        return scrollPane;
    }
}
