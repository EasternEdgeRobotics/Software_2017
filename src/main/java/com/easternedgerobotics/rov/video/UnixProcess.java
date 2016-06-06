package com.easternedgerobotics.rov.video;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.lang.reflect.Field;

final class UnixProcess {
    private static final Runtime RUNTIME = Runtime.getRuntime();

    /**
     * Executes the specified command and arguments in a separate process.
     * @param commands the command and arguments
     * @return an {@link UnixProcess} representing the command
     */
    static UnixProcess start(final String... commands) {
        try {
            return new UnixProcess(pid(RUNTIME.exec(commands)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long pid(final Process process) {
        try {
            final Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            final Object value = pid.get(process);
            return ((Integer) value).longValue();
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            Logger.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * The pid of the process.
     */
    private final long pid;

    private UnixProcess(final long pid) {
        Logger.info("Created process with PID {}", pid);
        this.pid = pid;
    }

    void kill() {
        signal("SIGTERM");
    }

    @SuppressWarnings("SpellCheckingInspection")
    void sigusr1() {
        signal("SIGUSR1");
    }

    private void signal(final String signal) {
        try {
            RUNTIME.exec(String.format("kill -%s %d", signal, pid));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
