package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.config.DistanceCalculatorConfig;
import com.easternedgerobotics.rov.io.FileUtil;
import com.easternedgerobotics.rov.video.CameraCalibration;

import javafx.scene.image.Image;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.javafx.sources.Change;
import rx.javafx.sources.Flag;
import rx.javafx.sources.ListChange;
import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import javax.inject.Inject;

public final class DistanceCalculatorViewController implements ViewController {
    /**
     * The subscriptions held by this controller.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * Holds onto subscriptions of the window size.
     */
    private final CompositeSubscription resizeSubscription = new CompositeSubscription();

    /**
     * The label of the open image option.
     */
    private static final String OPEN_ACTION = "Open";

    /**
     * The label of the delete image option.
     */
    private static final String DELETE_ACTION = "Delete";

    /**
     * The view controlled by this instance.
     */
    private final DistanceCalculatorView view;

    /**
     * A gallery of undistorted images.
     */
    private final GalleryView gallery;

    /**
     * The calibration object used to create undistorted images.
     */
    private final CameraCalibration cameraCalibration;

    /**
     * The config associated with the distance calculator.
     */
    private final DistanceCalculatorConfig config;

    /**
     * Control the distance calculator using this controller.
     *
     * @param view The view controlled by this instance.
     * @param gallery A gallery of undistorted images.
     * @param cameraCalibration The calibration object used to create undistorted images.
     * @param config The config associated with the distance calculator.
     */
    @Inject
    public DistanceCalculatorViewController(
        final DistanceCalculatorView view,
        final GalleryView gallery,
        final CameraCalibration cameraCalibration,
        @Configurable("distanceCalculator") final DistanceCalculatorConfig config
    ) {
        this.view = view;
        this.gallery = gallery;
        this.cameraCalibration = cameraCalibration;
        this.config = config;
    }

    @Override
    public void onCreate() {
        final Observable<ListChange<GalleryImageView>> images =
            JavaFxObservable.changesOf(gallery.imageViews);

        subscriptions.add(images
            .filter(change -> change.getFlag().equals(Flag.ADDED))
            .map(ListChange::getValue)
            .flatMap(iv -> iv.doubleClickAction(OPEN_ACTION)
                .takeUntil(iv.menuAction(DELETE_ACTION))
                .doOnCompleted(() -> iv.getPath().toFile().delete()))
            .subscribe(this::setImageFromPath));

        gallery.actions.addAll(Arrays.asList(OPEN_ACTION, DELETE_ACTION));
        gallery.directoryLabel.setText(config.imageDirectory());
        view.galleryBorderPane.setCenter(gallery.getParent());

        subscriptions.add(JavaFxObservable.valuesOf(view.captureA.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureUndistortedImageA(
                FileUtil.nextName(config.imageDirectory(), "undistortedA", "png"))));

        subscriptions.add(JavaFxObservable.valuesOf(view.captureB.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureUndistortedImageB(
                FileUtil.nextName(config.imageDirectory(), "undistortedB", "png"))));
    }

    /**
     * Set the calculator to display an image from the following path, and resize this image if the windows changes.
     *
     * @param path
     */
    public void setImageFromPath(final Path path) {
        resizeSubscription.clear();
        try {
            final File tempFile = File.createTempFile(path.toFile().getName(), ".tmp");
            Files.copy(path, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            final Image image = new Image(new FileInputStream(tempFile));
            resizeSubscription.add(Observable
                .merge(
                    JavaFxObservable.changesOf(view.borderPane.widthProperty()),
                    JavaFxObservable.changesOf(view.borderPane.heightProperty()))
                .map(Change::getNewVal)
                .startWith(0)
                .subscribe(v -> view.setImage(image)));
        } catch (final IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        resizeSubscription.unsubscribe();
    }
}
