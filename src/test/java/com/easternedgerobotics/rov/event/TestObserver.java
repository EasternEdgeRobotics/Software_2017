package com.easternedgerobotics.rov.event;

import rx.Observer;

import java.util.concurrent.CountDownLatch;

class TestObserver<E> implements Observer<E> {
    private final CountDownLatch latch;

    private E value;

    private boolean aborted;

    private Throwable error;

    TestObserver(final int count) {
        latch = new CountDownLatch(count);
        aborted = false;
    }

    @Override
    public void onNext(final E v) {
        value = v;
        latch.countDown();
    }

    @Override
    public void onError(final Throwable e) {
        error = e;
        aborted = true;
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }

    public E getValue() {
        return value;
    }

    /**
     * Waits for the observer to complete or abort.
     */
    public void await() {
        try {
            latch.await();
        } catch (final InterruptedException e) {
            onError(e);
        }

        if (aborted) {
            throw new RuntimeException(error);
        }
    }
}
