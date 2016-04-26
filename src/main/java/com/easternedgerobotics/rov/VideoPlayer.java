package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.io.HumbleVideoDecoder;
import com.easternedgerobotics.rov.swing.VideoFrame;

import io.humble.video.customio.InputOutputStreamHandler;
import io.humble.video.customio.URLProtocolManager;
import rx.Subscription;
import rx.observables.SwingObservable;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import javax.swing.SwingUtilities;

public final class VideoPlayer {
    private VideoPlayer() {

    }

    private static final int X = 1920;

    private static final int Y = 1080;

    public static void main(final String[] args) throws IOException, InterruptedException {
        System.setProperty("sun.java2d.opengl", "true");
        final URLProtocolManager manager = URLProtocolManager.getManager();
        manager.registerFactory("tcp", (final String protocol, final String sURL, final int flags) -> {
            try {
                System.out.println("Waiting for " + sURL);
                final URI url = new URI(sURL);
                final Socket socket = (new ServerSocket(url.getPort())).accept();
                System.out.println("Connection made for " + sURL);
                return new InputOutputStreamHandler(socket.getInputStream());
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });

        SwingUtilities.invokeLater(() -> {
            final VideoFrame videoFrame = new VideoFrame(1, 1);
            videoFrame.setSize(X, Y);
            videoFrame.setVisible(true);

            final Subscription videoStream1 = HumbleVideoDecoder.make("tcp://server1:5001")
                .flatMap(decoder -> decoder.frames())
                .subscribe(videoFrame.updateImageByIndex(0)::accept);

            SwingObservable.fromWindowEventsOf(videoFrame)
                .filter((final WindowEvent e) -> (e.getID() == WindowEvent.WINDOW_CLOSING))
                .first()
                .subscribe(e -> {
                    videoStream1.unsubscribe();
                });
        });
    }
}
