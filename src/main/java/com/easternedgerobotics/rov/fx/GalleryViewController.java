package com.easternedgerobotics.rov.fx;

import javafx.event.EventType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.pmw.tinylog.Logger;
import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.inject.Inject;

public final class GalleryViewController implements ViewController {
    private final CompositeSubscription imageViewSubscriptions = new CompositeSubscription();

    private final GalleryView view;

    @Inject
    public GalleryViewController(final GalleryView view) {
        this.view = view;
    }

    @Override
    public void onCreate() {
        view.updateFolder.subscribe(view.folderLabel::setText);
        view.updateFolder.subscribe(this::changed);
    }

    @Override
    public void onDestroy() {
        imageViewSubscriptions.unsubscribe();
    }

    private void changed(final String current) {
        final File folder = new File(current);
        if ((folder.exists() || folder.mkdirs()) && folder.isDirectory()) {
            loadFolder(folder);
        }
    }

    private void loadFolder(final File folder) {
        imageViewSubscriptions.clear();
        view.tilePane.getChildren().removeAll(view.imageViews);
        view.imageViews.clear();

        final File[] folderContent = folder.listFiles();
        if (folderContent == null) {
            Logger.error(new FileNotFoundException("No files found in folder: " + folder.toString()));
            return;
        }

        for (final File file : folderContent) {
            try {
                final File tempFile = File.createTempFile(file.getName(), ".tmp");
                Files.copy(file.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                final Image image = new Image(new FileInputStream(tempFile), GalleryView.IMAGE_WIDTH, 0, true, true);
                final ImageView imageView = new ImageView(image);
                imageView.setFitWidth(GalleryView.IMAGE_WIDTH);
                view.imageViews.add(imageView);

                imageViewSubscriptions.add(JavaFxObservable
                    .eventsOf(imageView, EventType.ROOT)
                    .filter(event -> event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                    .cast(MouseEvent.class)
                    .filter(event -> event.getButton().equals(MouseButton.PRIMARY))
                    .filter(event -> event.getClickCount() == 2)
                    .subscribe(event -> view.selected.onNext(tempFile)));

                final MenuItem open = new MenuItem("Open");
                imageViewSubscriptions.add(JavaFxObservable
                    .actionEventsOf(open)
                    .subscribe(event -> view.selected.onNext(tempFile)));

                final MenuItem delete = new MenuItem("Delete");
                imageViewSubscriptions.add(JavaFxObservable
                    .actionEventsOf(delete)
                    .subscribe(event -> removeImage(file, imageView)));

                final ContextMenu contextMenu = new ContextMenu();
                contextMenu.getItems().addAll(open, delete);

                imageViewSubscriptions.add(JavaFxObservable
                    .eventsOf(imageView, EventType.ROOT)
                    .filter(event -> event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                    .cast(MouseEvent.class)
                    .filter(event -> event.getButton().equals(MouseButton.SECONDARY))
                    .subscribe(event -> contextMenu.show(imageView, event.getScreenX(), event.getScreenY())));

            } catch (final IOException e) {
                Logger.error(e);
            }
        }
        view.tilePane.getChildren().addAll(view.imageViews);
    }

    private void removeImage(final File file, final ImageView imageView) {
        if (file.exists()) {
            file.delete();
        }
        view.tilePane.getChildren().remove(imageView);
    }
}
