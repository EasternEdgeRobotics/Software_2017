package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.video.CameraCalibration;

import rx.Observable;
import rx.javafx.sources.Flag;
import rx.javafx.sources.ListChange;
import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import java.util.Arrays;
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
     * The calibration object associated with this view.
     */
    private final CameraCalibration cameraCalibration;

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
     * @param cameraCalibration The calibration object associated with this view.
     * @param config The configuration of the camera calibrator.
     */
    @Inject
    public CameraCalibrationViewController(
        final CameraCalibrationView view,
        final GalleryView cameraACalibration,
        final GalleryView cameraBCalibration,
        final CameraCalibration cameraCalibration,
        @Configurable("cameraCalibration") final CameraCalibrationConfig config
    ) {
        this.view = view;
        this.cameraACalibration = cameraACalibration;
        this.cameraBCalibration = cameraBCalibration;
        this.cameraCalibration = cameraCalibration;
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

        subscriptions.add(JavaFxObservable.valuesOf(view.captureCalibrateA.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureCalibrationImageA()));

        subscriptions.add(JavaFxObservable.valuesOf(view.captureCalibrateB.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureCalibrationImageB()));

        subscriptions.add(JavaFxObservable.valuesOf(view.calibrate.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.calibrate()));
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
    }
}
