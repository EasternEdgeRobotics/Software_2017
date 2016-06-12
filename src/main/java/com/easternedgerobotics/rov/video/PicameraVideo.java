package com.easternedgerobotics.rov.video;

/**
 * The {@code PicameraVideo} class represents a external video process that transmits its video to a known recipient.
 */
public final class PicameraVideo {
    /**
     * The address of the video player.
     */
    private final String host;

    /**
     * The port on the destination for the video player.
     */
    private final int port;

    /**
     * The external video process.
     */
    private UnixProcess process;

    /**
     * Constructs a new {@code PicameraVideo} instance.
     * @param host the address of the host of the video player
     * @param port the port on the host of the video player
     */
    public PicameraVideo(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Start transmitting the video feed.
     */
    public final void start() {
        process = UnixProcess.start("eer-camera", host, String.valueOf(port));
    }

    /**
     * Flip the video feed.
     */
    public final void flip() {
        if (process == null) {
            throw new IllegalStateException("The process must be started before its video can be flipped.");
        }

        process.sigusr1();
    }

    /**
     * Stop the video feed.
     */
    public final void stop() {
        if (process == null) {
            throw new IllegalStateException("The process must be started before it can be killed.");
        }

        process.kill();
        process = null;
    }
}
