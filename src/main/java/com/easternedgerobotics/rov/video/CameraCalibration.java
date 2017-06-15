package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.files.DirectoryUtil;
import com.easternedgerobotics.rov.io.files.FileUtil;
import com.easternedgerobotics.rov.io.files.ValueStore;
import com.easternedgerobotics.rov.value.CameraCalibrationValue;
import com.easternedgerobotics.rov.value.CameraCaptureValueA;
import com.easternedgerobotics.rov.value.CameraCaptureValueB;

import rx.Scheduler;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class CameraCalibration {
    /**
     * The value store for calibration results.
     */
    private final ValueStore<CameraCalibrationValue> store;

    /**
     * The network event publisher to communicate with the cameras.
     */
    private final EventPublisher eventPublisher;

    /**
     * The calibration settings.
     */
    private final CameraCalibrationConfig config;

    /**
     * The scheduler used when performing calibration work.
     */
    private final Scheduler scheduler;

    /**
     * Create a calibration object which can perform a chessboard calibration.
     *
     * @param eventPublisher The network event publisher to communicate with the cameras.
     * @param config         The calibration settings.
     * @param store          The value store for calibration results.
     * @param scheduler      The scheduler used when performing calibration work.
     */
    public CameraCalibration(
        final EventPublisher eventPublisher,
        final CameraCalibrationConfig config,
        final ValueStore<CameraCalibrationValue> store,
        final Scheduler scheduler
    ) {
        this.eventPublisher = eventPublisher;
        this.config = config;
        this.store = store;
        this.scheduler = scheduler;
    }

    /**
     * Run a camera calibration on each of the cameras using the images in the calibration folder.
     * All images which pass the calibration will be moved to the valid folder defined in the config.
     */
    public void calibrate() {
        final Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(() -> ChessboardCalibration.findCameraCalibrationResult(
            config.cameraAImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
        ).ifPresent(value -> store.set(config.cameraAName(), value)));
        worker.schedule(() -> ChessboardCalibration.findCameraCalibrationResult(
            config.cameraBImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
        ).ifPresent(value -> store.set(config.cameraBName(), value)));
    }

    /**
     * Capture an image to be used with the camera A calibration.
     * Timeout for 10 second in case the stream is not initialized.
     */
    public void captureCalibrationImageA() {
        final File destination = FileUtil.nextName(config.cameraAImagesDirectory(), "checkerboardA", "png");
        if (destination != null) {
            eventPublisher.emit(new CameraCaptureValueA(destination.getAbsolutePath()));
        }
    }

    /**
     * Capture an image to be used with the camera B calibration.
     * Timeout for 10 second in case the stream is not initialized.
     */
    public void captureCalibrationImageB() {
        final File destination = FileUtil.nextName(config.cameraBImagesDirectory(), "checkerboardB", "png");
        if (destination != null) {
            eventPublisher.emit(new CameraCaptureValueB(destination.getAbsolutePath()));
        }
    }

    /**
     * Capture an image to be used with the camera A calibration.
     * Timeout for 1 minute in case the stream is not initialized.
     *
     * @param saveFile the output destination
     */
    public void captureUndistortedImageA(final File saveFile) {
        store.get(config.cameraAName()).map(DistortionCorrector::new).ifPresent(corrector -> {
            final File destination = FileUtil.nextName(config.cameraAPreUndistortedDirectory(), "captureA", "png");
            if (destination != null) {
                DirectoryUtil.observe(Paths.get(config.cameraAPreUndistortedDirectory()))
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .filter(destination.getAbsoluteFile().toPath()::equals).take(1)
                    .timeout(1, TimeUnit.MINUTES)
                    .delay(1, TimeUnit.SECONDS)
                    .subscribe(path -> corrector.apply(destination, saveFile));
                eventPublisher.emit(new CameraCaptureValueA(destination.getAbsolutePath()));
            }
        });
    }

    /**
     * Capture an image to be used with the camera B calibration.
     * Timeout for 1 minute in case the stream is not initialized.
     *
     * @param saveFile the output destination
     */
    public void captureUndistortedImageB(final File saveFile) {
        store.get(config.cameraBName()).map(DistortionCorrector::new).ifPresent(corrector -> {
            final File destination = FileUtil.nextName(config.cameraBPreUndistortedDirectory(), "captureB", "png");
            if (destination != null) {
                DirectoryUtil.observe(Paths.get(config.cameraBPreUndistortedDirectory()))
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .filter(destination.getAbsoluteFile().toPath()::equals).take(1)
                    .timeout(1, TimeUnit.MINUTES)
                    .delay(1, TimeUnit.SECONDS)
                    .subscribe(path -> corrector.apply(destination, saveFile));
                eventPublisher.emit(new CameraCaptureValueB(destination.getAbsolutePath()));
            }
        });
    }
}
