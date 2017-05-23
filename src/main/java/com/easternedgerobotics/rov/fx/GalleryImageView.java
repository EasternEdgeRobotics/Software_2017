package com.easternedgerobotics.rov.fx;

import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GalleryImageView extends ImageView {
    /**
     * The subscription held by the menu.
     */
    private final Subscription subscription;

    /**
     * Offset to leave some room for the user to move the fullscreen window.
     */
    private static final int IMAGE_OFFSET = 10;

    /**
     * The size of the image thumbnail.
     */
    private static final int PREVIEW_WIDTH = 150;

    /**
     * The path associated with this image.
     */
    private final Path path;

    /**
     * The menu items displayed when the view is right clicked.
     */
    private final Map<String, MenuItem> menuItems;

    /**
     * Create a {@code GalleryImageView} object to display an image thumbnail.
     *
     * @param path the path of the image.
     * @param actions the actions which can be performed.
     * @throws IOException
     */
    public GalleryImageView(final Path path, final List<String> actions) throws IOException {
        super(openImageThumbnailNonBlocking(path));
        this.path = path;
        setFitWidth(PREVIEW_WIDTH);

        final ContextMenu contextMenu = new ContextMenu();
        menuItems = actions.stream().collect(Collectors.toMap(Function.identity(), MenuItem::new));
        contextMenu.getItems().addAll(menuItems.values());

        subscription = JavaFxObservable.eventsOf(this, EventType.ROOT)
            .filter(event -> event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
            .cast(MouseEvent.class)
            .filter(event -> event.getButton().equals(MouseButton.SECONDARY))
            .subscribe(event -> contextMenu.show(this, event.getScreenX(), event.getScreenY()));
    }

    /**
     * Clean up existing subscriptions.
     */
    public void onDestroy() {
        subscription.unsubscribe();
    }

    /**
     * The path associated with this image.
     *
     * @return the path.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Observe the path when a double click occurs, and when a particular menu item is selected.
     *
     * @param action the menu item associated with double click actions.
     * @return an observable of the images path.
     */
    public Observable<Path> doubleClickAction(final String action) {
        return menuAction(action).mergeWith(
            JavaFxObservable.eventsOf(this, EventType.ROOT)
                .filter(event -> event.getEventType().equals(MouseEvent.MOUSE_CLICKED)
                    && ((MouseEvent) event).getButton().equals(MouseButton.PRIMARY)
                    && ((MouseEvent) event).getClickCount() == 2)
                .map(e -> path));
    }

    /**
     * Observe the path when a particular menu item is selected.
     *
     * @param action the menu item associated with this action.
     * @return an observable of the images path.
     */
    public Observable<Path> menuAction(final String action) {
        return JavaFxObservable.actionEventsOf(menuItems.get(action)).map(e -> path);
    }

    /**
     * Display the image associated with this view as a fullscreen image.
     */
    public void fullscreen() {
        final ImageView imageView;
        try {
            final Image image = openImageNonBlocking(path);
            imageView = new ImageView(image);
        } catch (final IOException e) {
            Logger.error(e);
            return;
        }

        imageView.setImage(imageView.getImage());
        imageView.setStyle("-fx-background-color: BLACK");
        imageView.setFitHeight(Screen.getPrimary().getVisualBounds().getHeight() - IMAGE_OFFSET);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(imageView);
        borderPane.setStyle("-fx-background-color: BLACK");

        final Stage stage = new Stage();

        // Only one screen will ever be in a 1x1 block at the current location.
        final Point location = MouseInfo.getPointerInfo().getLocation();
        final Screen currentScreen = Screen.getScreensForRectangle(
            location.getX(), location.getY(), 1, 1).toArray(new Screen[1])[0];

        // Set image to full screen in the active screen
        stage.setWidth(currentScreen.getVisualBounds().getWidth());
        stage.setHeight(currentScreen.getVisualBounds().getHeight());
        stage.setX(currentScreen.getVisualBounds().getMinX());
        stage.setY(currentScreen.getVisualBounds().getMinY());

        final Scene scene = new Scene(borderPane, Color.BLACK);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Open an image thumbnail without claiming the system file lock.
     *
     * @param path the path to open
     * @return the image
     * @throws IOException if the image could not be opened.
     */
    private static Image openImageThumbnailNonBlocking(final Path path) throws IOException {
        final File tempFile = File.createTempFile(path.toFile().getName(), ".tmp");
        Files.copy(path, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return new Image(new FileInputStream(tempFile), PREVIEW_WIDTH, 0, true, true);
    }

    /**
     * Open an image without claiming the system file lock.
     *
     * @param path the path to open
     * @return the image
     * @throws IOException if the image could not be opened.
     */
    private static Image openImageNonBlocking(final Path path) throws IOException {
        final File tempFile = File.createTempFile(path.toFile().getName(), ".tmp");
        Files.copy(path, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return new Image(new FileInputStream(tempFile));
    }
}
