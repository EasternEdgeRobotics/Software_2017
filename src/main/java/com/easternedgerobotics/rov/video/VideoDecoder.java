package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.VideoDecoderConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.value.VideoValueB;

import javafx.scene.image.Image;
import rx.Observable;

public final class VideoDecoder {
    private final EventPublisher eventPublisher;

    private final VideoDecoderConfig config;

    private final FFmpegFXImageDecoder decoderA;

    private final FFmpegFXImageDecoder decoderB;

    public VideoDecoder(
        final EventPublisher eventPublisher,
        final VideoDecoderConfig config
    ) {
        this.eventPublisher = eventPublisher;
        this.config = config;
        this.decoderA = new FFmpegFXImageDecoder(
            config.portA(),
            config.format(),
            config.frameRate(),
            config.socketBacklog(),
            config.bitrate(),
            config.preset(),
            config.numBuffers(),
            config.introVideoLocation());
        this.decoderB  = new FFmpegFXImageDecoder(
            config.portB(),
            config.format(),
            config.frameRate(),
            config.socketBacklog(),
            config.bitrate(),
            config.preset(),
            config.numBuffers(),
            config.introVideoLocation());
    }

    public void start() {
        decoderA.start();
        decoderB.start();
        eventPublisher.emit(new VideoValueA(config.host(), config.portA()));
        eventPublisher.emit(new VideoValueB(config.host(), config.portB()));
    }

    public void stop() {
        decoderA.stop();
        decoderB.stop();
    }

    public void restart() {
        stop();
        start();
    }

    public Observable<Image> cameraAImages() {
        return decoderA.getImages();
    }

    public Observable<Image> cameraBImages() {
        return decoderB.getImages();
    }
}
