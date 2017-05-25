package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.CameraCalibrationConfig;
import com.easternedgerobotics.rov.io.FileUtil;

import org.pmw.tinylog.Logger;
import rx.Scheduler;

import java.io.File;
import java.io.IOException;
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
     * The source of the video frames to be calibrated.
     */
    private final VideoDecoder videoDecoder;

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
     * @param videoDecoder The source of the video frames to be calibrated.
     * @param config       The calibration settings.
     * @param scheduler    The scheduler used when performing calibration work.
     */
    public CameraCalibration(
        final VideoDecoder videoDecoder,
        final CameraCalibrationConfig config,
        final Scheduler scheduler
    ) {
        this.videoDecoder = videoDecoder;
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
     * Timeout for 1 second in case the stream is not initialized.
     */
    public void captureCalibrationImageA() {
        videoDecoder.cameraAImages().observeOn(scheduler).timeout(1, TimeUnit.SECONDS).take(1).subscribe(image -> {
            final File outputFile = FileUtil.nextName(config.cameraAImagesDirectory(), "checkerboardA", "png");
            if (outputFile != null) {
                FileUtil.saveImageFile(image, outputFile, "png");
            }
        });
    }

    /**
     * Capture an image to be used with the camera B calibration.
     * Timeout for 1 second in case the stream is not initialized.
     */
    public void captureCalibrationImageB() {
        videoDecoder.cameraBImages().observeOn(scheduler).timeout(1, TimeUnit.SECONDS).take(1).subscribe(image -> {
            final File outputFile = FileUtil.nextName(config.cameraBImagesDirectory(), "checkerboardB", "png");
            if (outputFile != null) {
                FileUtil.saveImageFile(image, outputFile, "png");
            }
        });
    }

    /**
     * Capture an image to be used with the camera A calibration.
     * Timeout for 1 second in case the stream is not initialized.
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
        final DistortionCorrector corrector = correctorA.get();
        videoDecoder.cameraAImages().observeOn(scheduler).timeout(1, TimeUnit.SECONDS).take(1).subscribe(image -> {
            try {
                final File tempFile = File.createTempFile(saveFile.getName(), ".tmp");
                FileUtil.saveImageFile(image, tempFile, "png");
                corrector.apply(tempFile, saveFile);
            } catch (final IOException e) {
                Logger.error(e);
            }
        });
    }

    /**
     * Capture an image to be used with the camera B calibration.
     * Timeout for 1 second in case the stream is not initialized.
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
        final DistortionCorrector corrector = correctorB.get();
        videoDecoder.cameraBImages().observeOn(scheduler).timeout(1, TimeUnit.SECONDS).take(1).subscribe(image -> {
            try {
                final File tempFile = File.createTempFile(saveFile.getName(), ".tmp");
                FileUtil.saveImageFile(image, tempFile, "png");
                corrector.apply(tempFile, saveFile);
            } catch (final IOException e) {
                Logger.error(e);
            }
        });
    }
}
