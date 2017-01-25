package com.easternedgerobotics.rov.control;

import kotlin.Pair;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TaskManager {
    private final Scheduler clock;

    private final Observable<Long> interval;

    private final List<Runnable> initialStateSetters = new ArrayList<>();

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private volatile boolean isStarted;

    public TaskManager(final long sleepDuration, final TimeUnit unit, final Scheduler clock) {
        this.clock = clock;
        this.interval = Observable.interval(sleepDuration, unit, clock);
    }

    public void start() {
        isStarted = true;
    }

    public void stop() {
        isStarted = false;
        clock.createWorker().schedule(() -> initialStateSetters.forEach(Runnable::run));
    }

    public void unsubscribe() {
        stop();
        subscriptions.unsubscribe();
    }

    public <S> Observable<S> handle(final Supplier<S> supplier) {
        return Observable.<S>create(subscriber -> {
            subscriptions.add(interval.startWith(0L)
                .filter(t -> isStarted)
                .map(t -> supplier.get())
                .doOnError(Logger::warn)
                .subscribe(subscriber::onNext));
        }).share();
    }

    public <C, O extends C> void handle(
        final Observable<O> source,
        final O initial,
        final Consumer<C> consumer
    ) {
        initialStateSetters.add(() -> consumer.accept(initial));
        subscriptions.add(interval.startWith(0L)
            .withLatestFrom(source.startWith(initial), (t, v) -> v)
            .filter(v -> isStarted)
            .doOnError(Logger::warn)
            .subscribe(consumer::accept));
    }

    public <C, O extends C, S> Observable<S> handle(
        final Observable<O> source,
        final O initial,
        final Function<C, S> modifier
    ) {
        return Observable.<S>create(subscriber -> {
            initialStateSetters.add(() -> modifier.apply(initial));
            subscriptions.add(interval.startWith(0L)
                .withLatestFrom(source.startWith(initial), (t, v) -> v)
                .filter(v -> isStarted)
                .map(modifier::apply)
                .doOnError(Logger::warn)
                .subscribe(subscriber::onNext));
        }).share();
    }

    public <C1, O1 extends C1, C2, O2 extends C2, S> Observable<S> handle(
        final Observable<O1> source1, final O1 initial1,
        final Observable<O2> source2, final O2 initial2,
        final BiFunction<C1, C2, S> modifier
    ) {
        return Observable.<S>create(subscriber -> {
            initialStateSetters.add(() -> modifier.apply(initial1, initial2));
            subscriptions.add(interval.startWith(0L)
                .withLatestFrom(
                    Observable.combineLatest(source1.startWith(initial1), source2.startWith(initial2), Pair::new),
                    (t, v) -> v)
                .filter(v -> isStarted)
                .map(p -> modifier.apply(p.getFirst(), p.getSecond()))
                .doOnError(Logger::warn)
                .subscribe(subscriber::onNext));
        }).share();
    }
}
