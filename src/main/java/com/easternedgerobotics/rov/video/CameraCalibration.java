package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.DirectoryUtil;
import com.easternedgerobotics.rov.io.FileUtil;
import com.easternedgerobotics.rov.value.CameraCaptureValueA;
import com.easternedgerobotics.rov.value.CameraCaptureValueB;

import rx.Scheduler;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CameraCalibration {
    /**
     * Threadsafe holder for the calibration A result.
     */
    private final AtomicReference<CameraCalibrationResult> resultsA = new AtomicReference<>();

    /**
     * Threadsafe holder for the calibration B result.
     */
    private final AtomicReference<CameraCalibrationResult> resultsB = new AtomicReference<>();

    /**
     * Threadsafe holder for the calibration A distortion correction.
     */
    private final AtomicReference<DistortionCorrector> correctorA = new AtomicReference<>();

    /**
     * Threadsafe holder for the calibration B distortion correction.
     */
    private final AtomicReference<DistortionCorrector> correctorB = new AtomicReference<>();

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
     * @param scheduler      The scheduler used when performing calibration work.
     */
    public CameraCalibration(
        final EventPublisher eventPublisher,
        final CameraCalibrationConfig config,
        final Scheduler scheduler
    ) {
        this.eventPublisher = eventPublisher;
        this.config = config;
        this.scheduler = scheduler;
        scheduler.createWorker().schedule(() ->
            ChessboardCalibration.findCameraCalibrationResult(
                config.cameraAValidImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
            ).ifPresent(resultsA::set));
        scheduler.createWorker().schedule(() ->
            ChessboardCalibration.findCameraCalibrationResult(
                config.cameraBValidImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
            ).ifPresent(resultsB::set));
    }

    /**
     * Run a camera calibration on each of the cameras using the images in the calibration folder.
     * All images which pass the calibration will be moved to the valid folder defined in the config.
     */
    public void calibrate() {
        final Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(() -> {
            ChessboardCalibration.findCameraCalibrationResult(
                config.cameraAImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
            ).ifPresent(result -> {
                resultsA.set(result);
                FileUtil.copyFilesToDirectory(result.getValidFileNames(), config.cameraAValidImagesDirectory());
            });
        });
        worker.schedule(() -> {
            ChessboardCalibration.findCameraCalibrationResult(
                config.cameraBImagesDirectory(), config.chessboardWidth(), config.chessboardHeight()
            ).ifPresent(result -> {
                resultsB.set(result);
                FileUtil.copyFilesToDirectory(result.getValidFileNames(), config.cameraBValidImagesDirectory());
            });
        });
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
        if (correctorA.get() == null) {
            final CameraCalibrationResult result = resultsA.get();
            if (result == null) {
                return;
            }
            correctorA.set(new DistortionCorrector(result));
        }
        final File destination = FileUtil.nextName(config.cameraAPreUndistortedDirectory(), "captureA", "png");
        if (destination != null) {
            final DistortionCorrector corrector = correctorA.get();
            DirectoryUtil.observe(Paths.get(config.cameraAPreUndistortedDirectory()))
                .filter(destination.toPath()::equals).take(1)
                .timeout(1, TimeUnit.MINUTES)
                .observeOn(scheduler)
                .subscribe(path -> corrector.apply(destination, saveFile));
            eventPublisher.emit(new CameraCaptureValueA(destination.getAbsolutePath()));
        }
    }

    /**
     * Capture an image to be used with the camera B calibration.
     * Timeout for 1 minute in case the stream is not initialized.
     *
     * @param saveFile the output destination
     */
    public void captureUndistortedImageB(final File saveFile) {
        if (correctorB.get() == null) {
            final CameraCalibrationResult result = resultsB.get();
            if (result == null) {
                return;
            }
            correctorB.set(new DistortionCorrector(result));
        }
        final File destination = FileUtil.nextName(config.cameraBPreUndistortedDirectory(), "captureB", "png");
        if (destination != null) {
            final DistortionCorrector corrector = correctorB.get();
            DirectoryUtil.observe(Paths.get(config.cameraBPreUndistortedDirectory()))
                .filter(destination.toPath()::equals).take(1)
                .timeout(1, TimeUnit.MINUTES)
                .observeOn(scheduler)
                .subscribe(path -> corrector.apply(destination, saveFile));
            eventPublisher.emit(new CameraCaptureValueB(destination.getAbsolutePath()));
        }
    }
}
