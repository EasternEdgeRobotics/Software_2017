package com.easternedgerobotics.rov.control;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Timestamped;

import java.util.concurrent.atomic.AtomicLong;

public final class SupressObservable<T> {
    private final Observable<T> observable;

    private final Scheduler scheduler;

    private final AtomicLong lastSupress = new AtomicLong(0);

    public SupressObservable(final Observable<T> observable, final Scheduler scheduler, final long tick) {
        this.scheduler = scheduler;
        this.lastSupress.set(scheduler.now() - tick);
        this.observable = observable
            .observeOn(scheduler)
            .timestamp(scheduler)
            .filter(ts -> ts.getTimestampMillis() >= lastSupress.get() + tick)
            .map(Timestamped::getValue)
            .share();
    }

    public void supress() {
        lastSupress.set(scheduler.now());
    }

    public Observable<T> get() {
        return observable;
    }
}
