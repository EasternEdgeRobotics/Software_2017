package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.video.VideoDecoder;

import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public final class VideoViewController implements ViewController {
    private CompositeSubscription subscription = new CompositeSubscription();

    private final VideoDecoder decoder;

    private final VideoView view;

    @Inject
    public VideoViewController(
        final VideoDecoder decoder,
        final VideoView view
    ) {
        this.decoder = decoder;
        this.view = view;
    }

    @Override
    public void onCreate() {
        subscription.addAll(
            decoder.cameraAImages().observeOn(JAVA_FX_SCHEDULER).subscribe(view.cameraA::setImage),
            decoder.cameraBImages().observeOn(JAVA_FX_SCHEDULER).subscribe(view.cameraB::setImage));
    }

    @Override
    public void onDestroy() {
        subscription.clear();
    }
}
