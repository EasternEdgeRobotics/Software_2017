package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.config.VideoPlayerConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.value.VideoValueB;

import java.util.LinkedList;
import java.util.List;

public final class VideoPlayer {
    private final EventPublisher eventPublisher;

    /**
     * The address of this video player.
     */
    private final String host;

    private final VideoPlayerConfig config;

    private final List<UnixProcess> subprocesses;

    public VideoPlayer(final EventPublisher eventPublisher, final String host, final VideoPlayerConfig config) {
        this.eventPublisher = eventPublisher;
        this.host = host;
        this.config = config;
        this.subprocesses = new LinkedList<>();
    }

    public final void init() {
        subprocesses.add(UnixProcess.start("eer-video", String.valueOf(config.cameraAVideoPort())));
        subprocesses.add(UnixProcess.start("eer-video", String.valueOf(config.cameraBVideoPort())));
        eventPublisher.emit(new VideoValueA(host, config.cameraAVideoPort()));
        eventPublisher.emit(new VideoValueB(host, config.cameraBVideoPort()));
    }

    public final void stop() {
        subprocesses.forEach(UnixProcess::kill);
    }
}
