package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.io.DirectoryUtil;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import org.pmw.tinylog.Logger;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public final class GalleryViewController implements ViewController {
    /**
     * The subscriptions held by this controller.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * The view this controller manages.
     */
    private final GalleryView view;

    /**
     * Create a controller for a {@code GalleryView} which ensures that the {@code GalleryImageView#directoryLabel }
     * directory matches the tiled {@code GalleryImageView} objects in the view.
     *
     * @param view the view to be controlled.
     */
    @Inject
    public GalleryViewController(final GalleryView view) {
        this.view = view;
    }

    @Override
    public void onCreate() {
        view.directoryLabel.textProperty().addListener(this::updateDirectory);
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
    }

    /**
     * When the {@code GalleryImageView#directoryLabel } is changed this method resets the view and links
     * the tile pane to the directory.
     *
     * @param value The {@code ObservableValue} which value changed
     * @param prev The old value
     * @param curr The new value
     */
    private void updateDirectory(final ObservableValue<? extends String> value, final String prev, final String curr) {
        view.imageViews.clear();
        view.tilePane.getChildren().clear();
        subscriptions.clear();
        subscriptions.add(DirectoryUtil.observe(Paths.get(curr))
            .subscribeOn(Schedulers.newThread())
            .delay(1, TimeUnit.SECONDS)
            .subscribe(this::onImagePathChanged));
    }

    /**
     * When a change is detected in the directory, this method will add or remove that image from the view.
     *
     * @param path the path which has changed.
     */
    private void onImagePathChanged(final Path path) {
        final File file = path.toAbsolutePath().toFile();
        if (!file.exists()) {
            view.imageViews.stream().filter(iv -> iv.getPath().equals(path)).findFirst().ifPresent(iv -> {
                if (view.tilePane.getChildren().contains(iv)) {
                    Platform.runLater(() -> view.tilePane.getChildren().remove(iv));
                }
                if (view.imageViews.contains(iv)) {
                    view.imageViews.remove(iv);
                }
                iv.onDestroy();
            });
            return;
        }
        try {
            final GalleryImageView iv = new GalleryImageView(path, view.actions);
            Platform.runLater(() -> {
                view.tilePane.getChildren().add(iv);
                view.imageViews.add(iv);
            });
        } catch (final IOException e) {
            Logger.error(e);
        }
    }
}
