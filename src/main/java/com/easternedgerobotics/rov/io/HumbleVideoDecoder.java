package com.easternedgerobotics.rov.io;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class HumbleVideoDecoder implements VideoDecoder {
    /**
     * Creates and returns a VideoDecoder instance.
     *
     * @param filename the media resource locator to open
     * @return a VideoDecoder instance tied to the given MRL
     */
    public static Observable<VideoDecoder> make(final String filename) {
        final Observable<VideoDecoder> decoderObservable = Observable.create((subscriber) -> {
            try {
                final Demuxer demuxer = Demuxer.make();
                demuxer.open(filename, null, false, true, null, null);

                for (int i = 0; i < demuxer.getNumStreams(); i++) {
                    final DemuxerStream stream = demuxer.getStream(i);
                    final Decoder decoder = stream.getDecoder();

                    if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                        decoder.open(null, null);
                        subscriber.onNext(new HumbleVideoDecoder(demuxer, decoder, i));
                        subscriber.onCompleted();
                        return;
                    }
                }
            } catch (final IOException | InterruptedException ex) {
                subscriber.onError(ex);
            }
        });

        return decoderObservable.subscribeOn(Schedulers.io());
    }

    private final Demuxer demuxer;

    private final Decoder decoder;

    private final int streamId;

    HumbleVideoDecoder(final Demuxer videoDemuxer, final Decoder videoDecoder, final int videoStreamId) {
        demuxer = videoDemuxer;
        decoder = videoDecoder;
        streamId = videoStreamId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Observable<Image> frames() {
        final Observable<Image> frames = Observable.create(s -> decode(s));
        return frames.subscribeOn(Schedulers.io());
    }

    /**
     * Decode the video data for the given subscriber.
     *
     * This decoding loop is invoked when Observable.subscribe is called.
     *
     * @param subscriber The subscriber to the observable of frames
     */
    private final void decode(final Subscriber<? super Image> subscriber) {
        final MediaPicture mediaPicture = MediaPicture.make(
            decoder.getWidth(), decoder.getHeight(), decoder.getPixelFormat());

        final MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(
            MediaPictureConverterFactory.HUMBLE_BGR_24, mediaPicture);

        final MediaPacket packet = MediaPacket.make();

        try {
            // We reuse all of our objects to avoid reallocating
            // them. Each call to Humble resets objects to avoid
            // unnecessary reallocation.
            BufferedImage image = null;

            while (demuxer.read(packet) >= 0) {
                if (packet.getStreamIndex() != streamId) {
                    return;
                }

                int offset = 0;
                int bytesRead = 0;
                do {
                    bytesRead += decoder.decode(mediaPicture, packet, offset);
                    if (mediaPicture.isComplete()) {
                        image = converter.toImage(image, mediaPicture);
                        subscriber.onNext(image);
                    }
                    offset += bytesRead;
                } while (offset < packet.getSize());
            }

            do {
                decoder.decode(mediaPicture, null, 0);
                if (mediaPicture.isComplete()) {
                    image = converter.toImage(image, mediaPicture);
                    subscriber.onNext(image);
                }
            } while (mediaPicture.isComplete());
        } catch (final IOException e) {
            subscriber.onError(e);
            return;
        } catch (final InterruptedException e) {
            subscriber.onCompleted();
            return;
        }

        subscriber.onCompleted();
    }
}
