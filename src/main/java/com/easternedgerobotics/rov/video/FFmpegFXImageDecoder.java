package com.easternedgerobotics.rov.video;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class FFmpegFXImageDecoder {
    /**
     * Contain the subscription for the server observable.
     */
    private final CompositeSubscription subscription = new CompositeSubscription();

    private final String format;

    private final double frameRate;

    private final int bitrate;

    private final String preset;

    private final int numBuffers;

    private final PublishSubject<Image> images = PublishSubject.create();

    private final PublishSubject<Socket> server = PublishSubject.create();

    private final int port;

    private final int socketBacklog;

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
        this.socketBacklog = socketBacklog;
        this.format = format;
        this.frameRate = frameRate;
        this.bitrate = bitrate;
        this.preset = preset;
        this.numBuffers = numBuffers;
        Observable.create(new SocketSyncOnSubscribe())
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
            .subscribe(server::onNext);
    }

    void start() {
        subscription.add(server.take(1)
            .map(FFmpegSyncOnSubscribe::new)
            .flatMap(Observable::create)
            .subscribeOn(Schedulers.newThread())
            .subscribe(images::onNext, Logger::error));
    }

    void stop() {
        subscription.clear();
    }

    Observable<Image> getImages() {
        return images;
    }

    private final class SocketSyncOnSubscribe extends SyncOnSubscribe<ServerSocket, Socket> {
        @Override
        protected ServerSocket generateState() {
            try {
                return new ServerSocket(port, socketBacklog);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected ServerSocket next(final ServerSocket server, final Observer<? super Socket> observer) {
            if (server.isClosed()) {
                observer.onCompleted();
                return server;
            }
            try {
                observer.onNext(server.accept());
            } catch (final IOException e) {
                observer.onError(e);
            }
            return server;
        }

        @Override
        protected void onUnsubscribe(final ServerSocket server) {
            try {
                server.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class FFmpegSyncOnSubscribe extends SyncOnSubscribe<FrameGrabber, Image> {
        private Socket clientSocket;

        private final Java2DFrameConverter converter = new Java2DFrameConverter();

        private FFmpegSyncOnSubscribe(final Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        protected FrameGrabber generateState() {
            try {
                Logger.info(String.format("Processing stream from %s, %d",
                    clientSocket.getInetAddress(), clientSocket.getPort()));
                final FrameGrabber grabber = new FFmpegFrameGrabber(clientSocket.getInputStream());
                grabber.setFrameRate(frameRate);
                grabber.setFormat(format);
                grabber.setVideoBitrate(bitrate);
                grabber.setVideoOption("preset", preset);
                grabber.setNumBuffers(numBuffers);
                grabber.start();
                return grabber;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected FrameGrabber next(final FrameGrabber grabber, final Observer<? super Image> observer) {
            if (clientSocket.isClosed()) {
                observer.onCompleted();
                return grabber;
            }
            try {
                final Frame frame = grabber.grab();
                if (frame != null) {
                    final BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        observer.onNext(SwingFXUtils.toFXImage(bufferedImage, null));
                    }
                }
            } catch (final FrameGrabber.Exception e) {
                observer.onError(e);
            }
            return grabber;
        }

        @Override
        protected void onUnsubscribe(final FrameGrabber grabber) {
            try {
                grabber.close();
                clientSocket.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
