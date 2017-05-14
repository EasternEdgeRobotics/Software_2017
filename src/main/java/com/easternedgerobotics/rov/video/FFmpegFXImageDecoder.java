package com.easternedgerobotics.rov.video;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class FFmpegFXImageDecoder {
    private final int port;

    private final String format;

    private final double frameRate;

    private final int socketBacklog;

    private final int bitrate;

    private final String preset;

    private final int numBuffers;

    private final PublishSubject<Image> images = PublishSubject.create();

    private Thread thread;

    private ServerSocket server;

    FFmpegFXImageDecoder(
        final int port,
        final String format,
        final double frameRate,
        final int socketBacklog,
        final int bitrate,
        final String preset,
        final int numBuffers
    ) {
        this.port = port;
        this.format = format;
        this.frameRate = frameRate;
        this.socketBacklog = socketBacklog;
        this.bitrate = bitrate;
        this.preset = preset;
        this.numBuffers = numBuffers;
    }

    void start() {
        thread = new Thread(this::collectImages);
        thread.setDaemon(true);
        thread.start();
    }

    void stop() {
        if (thread != null) {
            try {
                if (server != null) {
                    server.close();
                }
                thread.interrupt();
                thread.join();
            } catch (final InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
    }

    Observable<Image> getImages() {
        return images;
    }

    private void collectImages() {
        try {
            final Java2DFrameConverter converter = new Java2DFrameConverter();
            server = new ServerSocket(port, socketBacklog);
            try (final Socket clientSocket = server.accept();
                 final FrameGrabber grabber = new FFmpegFrameGrabber(clientSocket.getInputStream())
            ) {
                grabber.setFrameRate(frameRate);
                grabber.setFormat(format);
                grabber.setVideoBitrate(bitrate);
                grabber.setVideoOption("preset", preset);
                grabber.setNumBuffers(numBuffers);
                grabber.start();
                while (!Thread.interrupted()) {
                    final Frame frame = grabber.grab();
                    if (frame != null) {
                        final BufferedImage bufferedImage = converter.convert(frame);
                        if (bufferedImage != null) {
                            Platform.runLater(() ->
                                images.onNext(SwingFXUtils.toFXImage(bufferedImage, null)));
                        }
                    }
                }
            }
        } catch (final IOException e) {
            if (server != null) {
                try {
                    server.close();
                } catch (final IOException closeException) {
                    Logger.error(e);
                    Logger.error(closeException);
                }
            }
        }
    }
}
