package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.video.VideoDecoder;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.javafx.sources.Flag;
import rx.javafx.sources.ListChange;
import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.inject.Inject;

public final class CameraCalibrationViewController implements ViewController {
    /**
     * The subscriptions held by this controller.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * The label of the open image option.
     */
    static final String OPEN_ACTION = "Open";

    /**
     * The label of the delete image option.
     */
    static final String DELETE_ACTION = "Delete";

    /**
     * The parent view managed by this controller.
     */
    private final CameraCalibrationView view;

    /**
     * A gallery for camera A calibration images.
     */
    private final GalleryView cameraACalibration;

    /**
     * A gallery for camera B calibration images.
     */
    private final GalleryView cameraBCalibration;

    /**
     * The video decoder to grab images latest camera images from.
     */
    private final VideoDecoder videoDecoder;

    /**
     * The configuration of the camera calibrator.
     */
    private final CameraCalibrationConfig config;

    /**
     * Create a controller for the camera calibration utility which lets users take calibration image snapshots
     * and view if the images are of good quality by opening the images as fullscreen.
     *
     * @param view The parent view managed by this controller.
     * @param cameraACalibration A gallery for camera A calibration images.
     * @param cameraBCalibration A gallery for camera B calibration images.
     * @param videoDecoder The video decoder to grab images latest camera images from.
     * @param config The configuration of the camera calibrator.
     */
    @Inject
    public CameraCalibrationViewController(
        final CameraCalibrationView view,
        final GalleryView cameraACalibration,
        final GalleryView cameraBCalibration,
        final VideoDecoder videoDecoder,
        @Configurable("cameraCalibration") final CameraCalibrationConfig config
    ) {
        this.view = view;
        this.cameraACalibration = cameraACalibration;
        this.cameraBCalibration = cameraBCalibration;
        this.videoDecoder = videoDecoder;
        this.config = config;
    }

    @Override
    public void onCreate() {
        final Observable<ListChange<GalleryImageView>> images = Observable.merge(
            JavaFxObservable.changesOf(cameraACalibration.imageViews),
            JavaFxObservable.changesOf(cameraBCalibration.imageViews));

        subscriptions.add(images
            .filter(change -> change.getFlag().equals(Flag.ADDED))
            .map(ListChange::getValue)
            .flatMap(iv -> iv.doubleClickAction(OPEN_ACTION)
                .takeUntil(iv.menuAction(DELETE_ACTION))
                .doOnCompleted(() -> iv.getPath().toFile().delete())
                .map(p -> iv))
            .subscribe(GalleryImageView::fullscreen));

        cameraACalibration.actions.addAll(Arrays.asList(OPEN_ACTION, DELETE_ACTION));
        cameraACalibration.directoryLabel.setText(config.cameraAImagesDirectory());
        view.cameraACalibrationTab.setContent(cameraACalibration.getParent());

        cameraBCalibration.actions.addAll(Arrays.asList(OPEN_ACTION, DELETE_ACTION));
        cameraBCalibration.directoryLabel.setText(config.cameraBImagesDirectory());
        view.cameraBCalibrationTab.setContent(cameraBCalibration.getParent());

        subscriptions.add(JavaFxObservable.valuesOf(view.captureCalibrateA.pressedProperty()).filter(x -> !x)
            .withLatestFrom(videoDecoder.cameraAImages(), (x, v) -> v)
            .subscribe(this::saveImageA));

        subscriptions.add(JavaFxObservable.valuesOf(view.captureCalibrateB.pressedProperty()).filter(x -> !x)
            .withLatestFrom(videoDecoder.cameraBImages(), (x, v) -> v)
            .subscribe(this::saveImageB));
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
    }

    /**
     * Save an image under the camera A calibration directory.
     *
     * @param image the image to save.
     */
    private void saveImageA(final Image image) {
        final String folder = cameraACalibration.directoryLabel.getText();
        final File outputFile = fileOfNextAvailableName(folder, "checkerboardA", "png");
        if (outputFile != null) {
            saveImageFile(image, outputFile, "png");
        }
    }

    /**
     * Save an image under the camera B calibration directory.
     *
     * @param image the image to save.
     */
    private void saveImageB(final Image image) {
        final String folder = cameraBCalibration.directoryLabel.getText();
        final File outputFile = fileOfNextAvailableName(folder, "checkerboardB", "png");
        if (outputFile != null) {
            saveImageFile(image, outputFile, "png");
        }
    }

    /**
     * Given a folder and a base name, append a number to the file name until the filename is unique.
     *
     * @param folderName the desired folder
     * @param name the base file name
     * @param type the file extension
     * @return a unique file name
     */
    private static File fileOfNextAvailableName(final String folderName, final String name, final String type) {
        final File folder = new File(folderName);
        if (!folder.exists()) {
            return null;
        }
        for (int i = 0;; i++) {
            final File file = new File(folder, String.format("%s_%d.%s", name, i, type));
            if (!file.exists()) {
                return file;
            }
        }
    }

    /**
     * Save an image to a destination output file with the provided extension.
     *
     * @param image the image to save
     * @param outputFile the image destination
     * @param type the extension
     */
    private static void saveImageFile(final Image image, final File outputFile, final String type) {
        final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bufferedImage, type, outputFile);
        } catch (final IOException e) {
            Logger.error(e);
        }
    }
}
