package com.easternedgerobotics.rov.io.files;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.Queue;

public final class DirectoryUtil {
    private DirectoryUtil() {

    }

    /**
     * List the contents of a directory as a cold observable.
     *
     * @param directoryPath the directory to list
     * @return an observable of paths.
     */
    public static Observable<Path> list(final Path directoryPath) {
        final File directoryFile = directoryPath.toFile();
        if (directoryFile.exists()) {
            final File[] files = directoryFile.listFiles();
            if (files != null) {
                return Observable.from(files).map(File::toPath).map(Path::toAbsolutePath);
            }
        }
        return Observable.empty();
    }

    /**
     * Observe a directory on the system for added and deleted paths. Starts by returning the paths which are
     * present in the directory when this method is called.
     *
     * Currently only supports adding and deleting paths in the folder. Modifying the paths or changing
     * the filename of items in the directory will be considered undefined behaviour.
     *
     * Files can be retroactively tested for existence using the File api.
     *
     * @param directoryPath the path to watch.
     * @return an observable of added or removed paths.
     */
    public static Observable<Path> observe(final Path directoryPath) {
        final File directoryFile = directoryPath.toFile();
        if ((directoryFile.exists() || directoryFile.mkdirs()) && directoryFile.isDirectory()) {
            return Observable.create(new DirectoryWatcherSyncOnSubscribe(directoryPath)).startWith(list(directoryPath));
        }
        return Observable.empty();
    }

    /**
     * Recursively delete a directory.
     *
     * @param directoryPath the directory to delete.
     */
    public static void clearDirectory(final Path directoryPath) {
        list(directoryPath).forEach(path -> {
            final File file = path.toFile();
            if (file.isDirectory()) {
                clearDirectory(path);
            }
            file.delete();
        });
    }

    private static final class DirectoryWatcherSyncOnSubscribe extends SyncOnSubscribe<WatchService, Path> {
        /**
         * The Path to observe.
         */
        private final Path directory;

        /**
         * The backpressure support of this onsubscribe object.
         * Only one object can be emitted during a call to the next method.
         */
        private final Queue<Path> queue = new LinkedList<>();

        /**
         * Create a SyncOnSubscribe object which monitors a directory using the java WatchService api.
         * All paths returned from this class will be absolute paths. Currently only supports adding and deleting
         * paths in the directory. Modifying the paths or changing the filename of items in the directory
         * will be considered undefined behaviour.
         *
         * @param directory The Path to observe.
         */
        DirectoryWatcherSyncOnSubscribe(final Path directory) {
            this.directory = directory;
        }

        /**
         * Generate the initial WatchService to be used by this class.
         *
         * @return the initial state.
         */
        @Override
        protected WatchService generateState() {
            try {
                final WatchService watcher = FileSystems.getDefault().newWatchService();
                directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                return watcher;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Detect a change in the path and report this change to observers.
         *
         * @param watcher the watcher state.
         * @param observer the subscriber.
         * @return the next state.
         */
        @Override
        @SuppressWarnings("unchecked")
        protected WatchService next(
            final WatchService watcher,
            final Observer<? super Path> observer
        ) {
            if (queue.isEmpty()) {
                try {
                    final WatchKey key = watcher.take();
                    key.pollEvents().stream().forEach(event ->
                        queue.add(directory.resolve((Path) event.context()).toAbsolutePath()));
                    if (!key.reset()) {
                        observer.onCompleted();
                    }
                } catch (final InterruptedException e) {
                    observer.onError(e);
                }
            } else {
                observer.onNext(queue.poll());
            }
            return watcher;
        }
    }
}
