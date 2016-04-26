package com.easternedgerobotics.rov.io;

import rx.Observable;

import java.awt.Image;

public interface VideoDecoder {
    /**
     * Returns an observable sequence of video frames.
     *
     * @return the video frames
     */
    public Observable<Image> frames();
}
