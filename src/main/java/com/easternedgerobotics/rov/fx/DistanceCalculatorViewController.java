package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.config.Configurable;
import com.easternedgerobotics.rov.config.DistanceCalculatorConfig;
import com.easternedgerobotics.rov.fx.distance.AxisNode;
import com.easternedgerobotics.rov.fx.distance.ShapeScene;
import com.easternedgerobotics.rov.fx.distance.TextNode;
import com.easternedgerobotics.rov.io.files.FileUtil;
import com.easternedgerobotics.rov.math.DistanceCalculator;
import com.easternedgerobotics.rov.value.AxisValue;
import com.easternedgerobotics.rov.value.PointValue;
import com.easternedgerobotics.rov.video.CameraCalibration;

import javafx.event.EventType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;

public final class DistanceCalculatorViewController implements ViewController {
    /**
     * The subscriptions held by this controller.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * Holds onto subscriptions of the window size.
     */
    private final CompositeSubscription currentDistanceSubscriptions = new CompositeSubscription();

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
     * Holds the configuration of the camera calibration.
     */
    private final CameraCalibrationConfig cameraCalibrationConfig;

    /**
     * Performs the distance calculations on a set of image points.
     */
    private final DistanceCalculator distanceCalculator;

    /**
     * Holds the configuration of the distance calculator.
     */
    private final DistanceCalculatorConfig distanceCalculatorConfig;

    /**
     * Control the distance calculator using this controller.
     *
     * @param view The view controlled by this instance.
     * @param gallery A gallery of undistorted images.
     * @param cameraCalibration The calibration object used to create undistorted images.
     * @param cameraCalibrationConfig Holds the configuration of the camera calibration.
     * @param distanceCalculator The instance used to calculate distances
     * @param distanceCalculatorConfig Holds the configuration of the distance calculator.
     */
    @Inject
    public DistanceCalculatorViewController(
        final DistanceCalculatorView view,
        final GalleryView gallery,
        final CameraCalibration cameraCalibration,
        @Configurable("cameraCalibration") final CameraCalibrationConfig cameraCalibrationConfig,
        final DistanceCalculator distanceCalculator,
        @Configurable("distanceCalculator") final DistanceCalculatorConfig distanceCalculatorConfig
    ) {
        this.view = view;
        this.gallery = gallery;
        this.cameraCalibration = cameraCalibration;
        this.cameraCalibrationConfig = cameraCalibrationConfig;
        this.distanceCalculator = distanceCalculator;
        this.distanceCalculatorConfig = distanceCalculatorConfig;
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
        gallery.directoryLabel.setText(distanceCalculatorConfig.imageDirectory());
        view.galleryBorderPane.setCenter(gallery.getParent());

        subscriptions.add(JavaFxObservable.valuesOf(view.captureA.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureUndistortedImageA(
                FileUtil.nextName(distanceCalculatorConfig.imageDirectory(), "undistortedA", "png"))));

        subscriptions.add(JavaFxObservable.valuesOf(view.captureB.pressedProperty()).filter(x -> !x).skip(1)
            .subscribe(x -> cameraCalibration.captureUndistortedImageB(
                FileUtil.nextName(distanceCalculatorConfig.imageDirectory(), "undistortedB", "png"))));
    }

    /**
     * Set the calculator to display an image from the following path, and resize this image if the windows changes.
     *
     * @param path
     */
    public void setImageFromPath(final Path path) {
        currentDistanceSubscriptions.clear();
        view.imageStack.getChildren().clear();
        view.imageStack.getChildren().addAll(view.imageView);
        view.imagePoints.clear();

        final ShapeScene shapeScene = new ShapeScene(view.imageStack, view.imageView, currentDistanceSubscriptions);

        final Observable<Number> sizeChanged = Observable.merge(
            JavaFxObservable.changesOf(view.borderPane.getScene().widthProperty()),
            JavaFxObservable.changesOf(view.borderPane.getScene().heightProperty())
        ).map(Change::getNewVal).startWith(0);

        // On fullscreen some component will not be fully adjusted.
        // Delay 1 millisecond to allow value propagation.
        currentDistanceSubscriptions.add(sizeChanged
            .delay(1, TimeUnit.MILLISECONDS, JAVA_FX_SCHEDULER)
            .subscribe(v -> shapeScene.draw()));

        if (path.toString().contains("undistortedA") || path.toString().contains("undistortedB")) {
            final String cameraName;
            if (path.toString().contains("undistortedA")) {
                cameraName = cameraCalibrationConfig.cameraAName();
            } else {
                cameraName = cameraCalibrationConfig.cameraBName();
            }
            final Observable<Boolean> calcPress = JavaFxObservable.valuesOf(view.calculateButton.pressedProperty());
            currentDistanceSubscriptions.add(calcPress.filter(x -> !x).skip(1).subscribe(x -> {
                final String scalarString = view.xAxisLength.getText();
                final float scalar;
                try {
                    scalar = Float.parseFloat(scalarString);
                } catch (final NumberFormatException e) {
                    return;
                }
                final AxisValue axis = view.axisNode.get().getAxis();
                final List<PointValue> pixelPoints = view.imagePoints
                    .stream().map(TextNode::getPoint).collect(Collectors.toList());
                distanceCalculator.calculate(axis, pixelPoints, cameraName, scalar).ifPresent(objectPoints -> {
                    for (int i = 0; i < objectPoints.size(); i++) {
                        view.imagePoints.get(i).setText(String.format("(%.2f, %.2f)",
                            objectPoints.get(i).getX(), objectPoints.get(i).getY()));
                    }
                    shapeScene.draw();
                });
            }));
        }

        try {

            final File tempFile = File.createTempFile(path.toFile().getName(), ".tmp");
            Files.copy(path, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            final Image image = new Image(new FileInputStream(tempFile));
            currentDistanceSubscriptions.add(sizeChanged.subscribe(v -> {
                view.imageStack.getChildren().remove(view.imageView);
                view.setImage(image);
                view.imageStack.getChildren().add(0, view.imageView);
            }));

            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem axis = new MenuItem("Axis");
            final MenuItem point = new MenuItem("Point");
            contextMenu.getItems().addAll(axis, point);

            final Observable<MouseEvent> events = JavaFxObservable.eventsOf(view.imageView, EventType.ROOT)
                .filter(event -> event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                .cast(MouseEvent.class)
                .filter(event -> event.getButton().equals(MouseButton.SECONDARY))
                .doOnNext(event -> contextMenu.show(view.imageView, event.getScreenX(), event.getScreenY()))
                .share();

            currentDistanceSubscriptions.add(JavaFxObservable.actionEventsOf(axis).withLatestFrom(events, (a, e) -> e)
                .take(1).subscribe(event -> {
                    final AxisNode axisNode = new AxisNode();
                    view.axisNode.set(axisNode);
                    shapeScene.add(axisNode, event.getX(), event.getY());
                    contextMenu.getItems().remove(axis);
                }));

            currentDistanceSubscriptions.add(JavaFxObservable.actionEventsOf(point).withLatestFrom(events, (a, e) -> e)
                .subscribe(event -> {
                    final TextNode imagePoint = new TextNode(new Circle(7, Color.DEEPPINK));
                    view.imagePoints.add(imagePoint);
                    shapeScene.add(imagePoint, event.getX(), event.getY());
                    final ContextMenu pointMenu = new ContextMenu();
                    final MenuItem delete = new MenuItem("Delete");
                    pointMenu.getItems().addAll(delete);
                    currentDistanceSubscriptions.add(JavaFxObservable
                        .eventsOf(imagePoint.getHandleShape(), EventType.ROOT)
                        .filter(e -> e.getEventType().equals(MouseEvent.MOUSE_CLICKED))
                        .cast(MouseEvent.class)
                        .filter(e -> e.getButton().equals(MouseButton.SECONDARY))
                        .subscribe(e -> pointMenu.show(imagePoint.getHandleShape(), e.getScreenX(), e.getScreenY())));
                    currentDistanceSubscriptions.add(JavaFxObservable.actionEventsOf(delete).take(1).subscribe(e -> {
                        view.imagePoints.remove(imagePoint);
                        shapeScene.remove(imagePoint);
                    }));
                }));

        } catch (final IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        currentDistanceSubscriptions.unsubscribe();
    }
}
