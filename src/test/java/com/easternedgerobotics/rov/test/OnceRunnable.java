package com.easternedgerobotics.rov.test;

import java.util.concurrent.atomic.AtomicBoolean;

final class OnceRunnable implements Runnable {
    private final AtomicBoolean flag = new AtomicBoolean(false);

    private final Runnable runnable;

    OnceRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public final void run() {
        if (flag.get()) {
            return;
        }

        runnable.run();
        flag.set(true);
    }
}
