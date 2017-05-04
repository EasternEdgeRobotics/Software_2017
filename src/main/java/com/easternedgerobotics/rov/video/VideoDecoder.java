package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.VideoDecoderConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.value.VideoValueB;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class VideoDecoder {
    private final EventPublisher eventPublisher;

    private final VideoDecoderConfig config;

    private final PublishSubject<Image> imagesA = PublishSubject.create();

    private final PublishSubject<Image> imagesB = PublishSubject.create();

    private Thread threadA;

    private Thread threadB;

    private ServerSocket serverA;

    private ServerSocket serverB;

    public VideoDecoder(final EventPublisher eventPublisher, final VideoDecoderConfig config) {
        this.eventPublisher = eventPublisher;
        this.config = config;
    }

    public void start() {
        threadA = new Thread(this::collectImagesA);
        threadB = new Thread(this::collectImagesB);
        threadA.setDaemon(true);
        threadB.setDaemon(true);
        threadA.start();
        threadB.start();
        eventPublisher.emit(new VideoValueA(config.host(), config.cameraAVideoPort()));
        eventPublisher.emit(new VideoValueB(config.host(), config.cameraBVideoPort()));
    }

    public void stop() {
        if (threadA != null) {
            try {
                if (serverA != null) {
                    serverA.close();
                }
                threadA.interrupt();
                threadA.join();
            } catch (final InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
        if (threadB != null) {
            try {
                if (serverB != null) {
                    serverB.close();
                }
                threadB.interrupt();
                threadB.join();
            } catch (final InterruptedException | IOException e) {
                Logger.error(e);
            }
        }
    }

    public void restart() {
        stop();
        start();
    }

    public Observable<Image> cameraAImages() {
        return imagesA;
    }

    public Observable<Image> cameraBImages() {
        return imagesB;
    }

    private void collectImagesA() {
        try {
            serverA = new ServerSocket(config.cameraAVideoPort(), config.socketBacklog());
            runGrabber(serverA, imagesA);
        } catch (final IOException e) {
            if (serverA != null) {
                try {
                    serverA.close();
                } catch (final IOException closeException) {
                    Logger.error(e);
                    Logger.error(closeException);
                }
            }
        }
    }

    private void collectImagesB() {
        try {
            serverB = new ServerSocket(config.cameraBVideoPort(), config.socketBacklog());
            runGrabber(serverB, imagesB);
        } catch (final IOException e) {
            if (serverB != null) {
                try {
                    serverB.close();
                } catch (final IOException closeException) {
                    Logger.error(e);
                    Logger.error(closeException);
                }
            }
        }
    }

    private void runGrabber(
        final ServerSocket server,
        final Observer<Image> images
    ) throws IOException {
        final Java2DFrameConverter converter = new Java2DFrameConverter();
        try (final Socket clientSocket = server.accept();
             final FrameGrabber grabber = new FFmpegFrameGrabber(clientSocket.getInputStream())
        ) {
            grabber.setFrameRate(config.frameRate());
            grabber.setFormat(config.format());
            grabber.start();
            while (!Thread.interrupted()) {
                final Frame frame = grabber.grab().clone();
                if (frame != null) {
                    final BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        images.onNext(SwingFXUtils.toFXImage(bufferedImage, null));
                        grabber.flush();
                    }
                }
            }
        }
    }
}
