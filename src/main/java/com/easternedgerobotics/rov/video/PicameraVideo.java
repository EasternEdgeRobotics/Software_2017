package com.easternedgerobotics.rov.video;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Controls the eer-camera service from a static interface.
 */
public final class PicameraVideo {
    private PicameraVideo() {

    }

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final String SERVICE = "eer-camera.service";

    public static void addShutdownHook() {
        RUNTIME.addShutdownHook(new Thread(PicameraVideo::stop));
    }

    public static void stop() {
        Logger.debug("Stopping video");
        try {
            RUNTIME.exec(new String[] {"systemctl", "stop", SERVICE});
        } catch (final IOException e) {
            Logger.error(e);
        }
    }

    public static void start(final String host, final int port) {
        Logger.debug("Starting video");
        try (final PrintWriter out = new PrintWriter("/run/eer/camera-environment")) {
            out.println(String.format("host=%s\nport=%s", host, port));
            RUNTIME.exec(new String[] {"systemctl", "restart", SERVICE});
        } catch (final IOException e) {
            Logger.error(e);
        }
    }

    public static void flip() {
        Logger.debug("Flipping video");
        try {
            RUNTIME.exec(new String[] {"systemctl", "kill", SERVICE, "--signal=SIGUSR1"});
        } catch (final IOException e) {
            Logger.error(e);
        }
    }
}
