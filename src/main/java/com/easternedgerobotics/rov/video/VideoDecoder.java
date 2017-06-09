package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.VideoDecoderConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.value.VideoValueB;

import javafx.scene.image.Image;
import org.pmw.tinylog.Logger;
import rx.Observable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class VideoDecoder {
    private final EventPublisher eventPublisher;

    private final VideoDecoderConfig config;

    private final FFmpegFXImageDecoder decoderA;

    private final FFmpegFXImageDecoder decoderB;

    private final String broadcastIP;

    public VideoDecoder(
        final EventPublisher eventPublisher,
        final VideoDecoderConfig config,
        final String broadcast
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
        final int lastLocation = broadcast.lastIndexOf('.');
        broadcastIP = broadcast.substring(0, lastLocation - 1);
    }

    public void start() {
        decoderA.start();
        decoderB.start();
        boolean initialized = false;
        final List<String> ipAddresses = new ArrayList<String>();
        try {
            final Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();
            while (network.hasMoreElements()) {
                final Enumeration<InetAddress> inet = network.nextElement().getInetAddresses();
                while (inet.hasMoreElements()) {
                    ipAddresses.add(inet.nextElement().toString());
                }
            }
        } catch (final SocketException error) {
            Logger.error(error);
        }
        for (final String newAddress: ipAddresses) {
            final int lastLocation = newAddress.lastIndexOf('.');
            if (lastLocation >= 0) {
                final String testAddress = newAddress.substring(1, lastLocation - 1);
                if (broadcastIP.equals(testAddress)) {
                    eventPublisher.emit(new VideoValueA(newAddress, config.portA()));
                    eventPublisher.emit(new VideoValueB(newAddress, config.portB()));
                    initialized = true;
                    break;
                }
            }
        }
        if (!initialized) {
            Logger.warn("Could not detect the IP of the runtime system");
        }
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
