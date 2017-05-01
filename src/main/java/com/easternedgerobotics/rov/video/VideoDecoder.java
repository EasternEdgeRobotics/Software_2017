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

    public VideoDecoder(final EventPublisher eventPublisher, final VideoDecoderConfig config) {
        this.eventPublisher = eventPublisher;
        this.config = config;
    }

    public void start() {
        if (threadA != null) {
            threadA.interrupt();
        }
        if (threadB != null) {
            threadB.interrupt();
        }
        threadA = new Thread(() -> collectImages(config.cameraAVideoPort(), imagesA));
        threadB = new Thread(() -> collectImages(config.cameraBVideoPort(), imagesB));
        threadA.start();
        threadB.start();
        eventPublisher.emit(new VideoValueA(config.host(), config.cameraAVideoPort()));
        eventPublisher.emit(new VideoValueB(config.host(), config.cameraBVideoPort()));
    }

    public void stop() {
        threadA.interrupt();
        threadB.interrupt();
    }

    public Observable<Image> cameraAImages() {
        return imagesA;
    }

    public Observable<Image> cameraBImages() {
        return imagesB;
    }

    private void collectImages(final int port, final Observer<Image> images) {
        final Java2DFrameConverter converter = new Java2DFrameConverter();
        try (final ServerSocket server = new ServerSocket(port, config.socketBacklog());
            final Socket clientSocket = server.accept();
            final FrameGrabber grabber = new FFmpegFrameGrabber(clientSocket.getInputStream())
        ) {
            grabber.setFrameRate(config.frameRate());
            grabber.setFormat(config.format());
            grabber.start();

            while (!Thread.currentThread().isInterrupted()) {
                final Frame frame = grabber.grab();
                if (frame != null) {
                    final BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        images.onNext(SwingFXUtils.toFXImage(bufferedImage, null));
                    }
                }
            }
        } catch (final IOException e) {
            images.onError(e);
        }
    }
}
