package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.config.Configurable;

import com.easternedgerobotics.rov.video.VideoDecoder;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pmw.tinylog.Logger;
import rx.observables.JavaFxObservable;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;

public final class CameraCalibrationViewController implements ViewController {
    private static final int IMAGE_OFFSET = 10;

    private final CameraCalibrationView view;

    private final GalleryView cameraACalibration;

    private final GalleryView cameraBCalibration;

    private final VideoDecoder videoDecoder;

    private final CameraCalibrationConfig config;

    @Inject
    public CameraCalibrationViewController(
        final CameraCalibrationView view,
        final GalleryView cameraACalibration,
        final GalleryView cameraBCalibration,
        final VideoDecoder videoDecoder,
        @Configurable("cameraCalibration")final CameraCalibrationConfig config
    ) {
        this.view = view;
        this.cameraACalibration = cameraACalibration;
        this.cameraBCalibration = cameraBCalibration;
        this.videoDecoder = videoDecoder;
        this.config = config;
    }

    @Override
    public void onCreate() {
        final Tab cameraACalibrationTab = new Tab();
        cameraACalibration.updateFolder.onNext(config.cameraAImagesDirectory());
        cameraACalibrationTab.setText("Camera A Calibration");
        cameraACalibrationTab.setContent(cameraACalibration.getParent());
        cameraACalibrationTab.setClosable(false);

        final Tab cameraBCalibrationTab = new Tab();
        cameraBCalibration.updateFolder.onNext(config.cameraBImagesDirectory());
        cameraBCalibrationTab.setText("Camera B Calibration");
        cameraBCalibrationTab.setContent(cameraBCalibration.getParent());
        cameraBCalibrationTab.setClosable(false);

        view.tabPane.getTabs().addAll(cameraACalibrationTab, cameraBCalibrationTab);

        cameraACalibration.selected.subscribe(this::openImage);
        cameraBCalibration.selected.subscribe(this::openImage);

        JavaFxObservable.valuesOf(view.captureCalibrateA.pressedProperty()).filter(x -> !x)
            .withLatestFrom(videoDecoder.cameraAImages(), (x, v) -> v)
            .subscribe(this::saveImageA);

        JavaFxObservable.valuesOf(view.captureCalibrateB.pressedProperty()).filter(x -> !x)
            .withLatestFrom(videoDecoder.cameraBImages(), (x, v) -> v)
            .subscribe(this::saveImageB);
    }

    private void openImage(final File file) {
        final ImageView imageView;
        try {
            final Image image = new Image(new FileInputStream(file));
            imageView = new ImageView(image);
        } catch (final FileNotFoundException e) {
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

    private void saveImageA(final Image image) {
        final String folder = cameraACalibration.folderLabel.getText();
        final File outputFile = fileOfNextAvailableName(folder, "checkerboardA", "png");
        if (outputFile != null) {
            saveImageFile(image, outputFile, "png");
            cameraACalibration.updateFolder.onNext(folder);
        }
    }

    private void saveImageB(final Image image) {
        final String folder = cameraBCalibration.folderLabel.getText();
        final File outputFile = fileOfNextAvailableName(folder, "checkerboardB", "png");
        if (outputFile != null) {
            saveImageFile(image, outputFile, "png");
            cameraBCalibration.updateFolder.onNext(folder);
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
