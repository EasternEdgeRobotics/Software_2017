package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.video.VideoDecoder;

import javax.inject.Inject;

public final class VideoViewController implements ViewController {
    @Inject
    public VideoViewController(
        final VideoDecoder decoder,
        final VideoView view
    ) {
        decoder.cameraAImages()
            .subscribeOn(JAVA_FX_SCHEDULER)
            .subscribe(view.cameraA::setImage);
        decoder.cameraBImages()
            .subscribeOn(JAVA_FX_SCHEDULER)
            .subscribe(view.cameraB::setImage);
    }
}
