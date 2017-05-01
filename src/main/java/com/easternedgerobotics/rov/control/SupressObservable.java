package com.easternedgerobotics.rov.control;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Timestamped;

public final class SupressObservable<T> {
    private final Observable<T> observable;

    private final Scheduler scheduler;

    private volatile long lastSupress;

    public SupressObservable(final Observable<T> observable, final Scheduler scheduler, final long tick) {
        this.scheduler = scheduler;
        this.observable = observable
            .timestamp(scheduler)
            .filter(ts -> ts.getTimestampMillis() >= lastSupress + tick)
            .map(Timestamped::getValue)
            .share();
    }

    public void supress() {
        lastSupress = scheduler.now();
    }

    public Observable<T> get() {
        return observable;
    }
}
