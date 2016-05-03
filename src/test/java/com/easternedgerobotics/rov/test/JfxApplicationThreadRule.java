package com.easternedgerobotics.rov.test;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;

public final class JfxApplicationThreadRule implements TestRule {
    private final Runnable setup = new OnceRunnable(() -> {
        try {
            SwingUtilities.invokeAndWait(JFXPanel::new);
        } catch (final InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    });

    @Override
    public final Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setup.run();

                final Throwable error = evaluateOnApplicationThread(base);
                if (error != null) {
                    throw error;
                }
            }
        };
    }

    private Throwable evaluateOnApplicationThread(final Statement statement) {
        final AtomicReference<Throwable> error = new AtomicReference<>(null);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                statement.evaluate();
            } catch (final Throwable throwable) {
                error.set(throwable);
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (final InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return error.get();
    }
}
