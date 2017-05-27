package com.easternedgerobotics.rov.io;

import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class TcpFileReceiver {
    /**
     * Contain the subscription for the server observable.
     */
    private final CompositeSubscription subscription = new CompositeSubscription();

    /**
     * the socket backlog variable to be used in the server client listener.
     */
    private final int socketBacklog;

    /**
     * the port used to create the server client listener.
     */
    private final int port;

    /**
     * Run a server which will accept client connections and download files.
     * All connections begin with a string file path followed by the file content.
     * The file content will be saved to that file path.
     *
     * @param port the port to start the file receiver on.
     * @param socketBacklog the socket backlog for the server.
     */
    public TcpFileReceiver(
        final int port,
        final int socketBacklog
    ) {
        this.port = port;
        this.socketBacklog = socketBacklog;
    }

    /**
     * Begin downloading files from remote connections.
     */
    public void start() {
        final Observable<Path> source = Observable.create(new TcpFileReceiverSyncOnSubscribe());
        subscription.add(source.subscribeOn(Schedulers.newThread()).subscribe(Logger::info, Logger::error));
    }

    /**
     * Stop downloading files.
     */
    public void stop() {
        subscription.clear();
    }

    private final class TcpFileReceiverSyncOnSubscribe extends SyncOnSubscribe<ServerSocket, Path> {
        /**
         * Create the initial server connection for the observable.
         *
         * @return the receiver server
         */
        @Override
        protected ServerSocket generateState() {
            try {
                return new ServerSocket(port, socketBacklog);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Grab a client and download their file.
         *
         * @param server the receiver server.
         * @param observer the listener to this receiver.
         * @return the server
         */
        @Override
        protected ServerSocket next(final ServerSocket server, final Observer<? super Path> observer) {
            if (server.isClosed()) {
                observer.onCompleted();
                return server;
            }
            try {
                try (final Socket clientSocket = server.accept();
                     final InputStream inputStream = clientSocket.getInputStream();
                     final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                ) {
                    final String pathName = reader.readLine();
                    final Path outputPath = Paths.get(pathName);
                    // Dirty hack to avoid the fact that streaming to a new file is not an atomic operation.
                    // The file name is created then the data is loaded to it. This causes services such as
                    // WatchService to see the file before it is created. The tmp file copy is a lot faster
                    // than the tcp copy, so this prevents reading an incomplete outputPath file.
                    final Path temp = File.createTempFile("TcpFileReceiver", ".tmp").toPath();
                    Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(temp, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    observer.onNext(outputPath);
                }
            } catch (final IOException e) {
                observer.onError(e);
            }
            return server;
        }

        /**
         * When the object is unsubscribed, close the server connection.
         *
         * @param server the server associated with this instance.
         */
        protected void onUnsubscribe(final ServerSocket server) {
            try {
                server.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
