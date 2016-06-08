package com.easternedgerobotics.rov.video;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.value.VideoValueB;

import java.util.LinkedList;
import java.util.List;

public final class VideoPlayer {
    private static final int CAMERA_A_VIDEO_PORT = 12345;

    private static final int CAMERA_B_VIDEO_PORT = 12346;

    private final EventPublisher eventPublisher;

    /**
     * The address of this video player.
     */
    private final String host;

    private final List<UnixProcess> subprocesses;

    public VideoPlayer(final EventPublisher eventPublisher, final String host) {
        this.eventPublisher = eventPublisher;
        this.host = host;
        this.subprocesses = new LinkedList<>();
    }

    public final void init() {
        subprocesses.add(UnixProcess.start("eer-video", String.valueOf(CAMERA_A_VIDEO_PORT)));
        subprocesses.add(UnixProcess.start("eer-video", String.valueOf(CAMERA_B_VIDEO_PORT)));
        eventPublisher.emit(new VideoValueA(host, CAMERA_A_VIDEO_PORT));
        eventPublisher.emit(new VideoValueB(host, CAMERA_B_VIDEO_PORT));
    }

    public final void stop() {
        subprocesses.forEach(UnixProcess::kill);
    }
}
