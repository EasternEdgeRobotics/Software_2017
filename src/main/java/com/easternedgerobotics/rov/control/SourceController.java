package com.easternedgerobotics.rov.control;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Timestamped;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class SourceController {
    private static final long SUPPRESS_LEN = 2000;

    private SourceController() {

    }

    public static <T> CompositeSubscription manageMultiViewModel(
        final Observable<? extends T> external,
        final Consumer<? super T> onExternal,
        final Scheduler externalScheduler,
        final Observable<? extends T> internal,
        final Consumer<? super T> onInternal,
        final Scheduler internalScheduler
    ) {
        final SuppressObservable<? extends T> es = new SuppressObservable<>(external, externalScheduler, SUPPRESS_LEN);
        final SuppressObservable<? extends T> is = new SuppressObservable<>(internal, internalScheduler, SUPPRESS_LEN);

        final CompositeSubscription sub = new CompositeSubscription();
        sub.addAll(
            es.get().subscribe(v -> {
                is.supress();
                onExternal.accept(v);
            }),
            is.get().subscribe(v -> {
                es.supress();
                onInternal.accept(v);
            }));
        return sub;
    }

    private static final class SuppressObservable<T> {
        private final Observable<T> observable;

        private final Scheduler scheduler;

        private final AtomicLong lastSuppress = new AtomicLong(0);

        SuppressObservable(final Observable<T> observable, final Scheduler scheduler, final long tick) {
            this.scheduler = scheduler;
            this.lastSuppress.set(scheduler.now() - tick);
            this.observable = observable
                .observeOn(scheduler)
                .timestamp(scheduler)
                .filter(ts -> ts.getTimestampMillis() >= lastSuppress.get() + tick)
                .map(Timestamped::getValue)
                .share();
        }

        void supress() {
            lastSuppress.set(scheduler.now());
        }

        Observable<T> get() {
            return observable;
        }
    }
}
