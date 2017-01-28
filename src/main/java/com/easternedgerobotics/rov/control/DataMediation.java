package com.easternedgerobotics.rov.control;

import kotlin.Pair;
import org.pmw.tinylog.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DataMediation {
    /**
     * The scheduler for running tasks managed by a DataMediation instance.
     */
    private final Scheduler clock;

    /**
     * The base observable for scheduled events.
     */
    private final Observable<Long> interval;

    /**
     * Runnables which force the consumers to update with idle state values.
     */
    private final List<Runnable> idleStateSetters = new ArrayList<>();

    /**
     * Aggregate of all subscriptions created by this instance.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * The state of the mediator.
     */
    private final AtomicBoolean isActive = new AtomicBoolean(false);

    /**
     * Mediate how data is transferred between a source and a consumer. Starts in idle state.
     * If the mediation state is active, data will be transferred from source to consumer every sleepDuration.
     * If the mediation state is idle, consumers will receive their idle state values.
     *
     * @param sleepDuration wait this many units between transferring data.
     * @param unit the unit that sleepDuration waits.
     * @param clock the scheduler used for performing data transfers.
     */
    public DataMediation(final long sleepDuration, final TimeUnit unit, final Scheduler clock) {
        this.clock = clock;
        this.interval = Observable.interval(sleepDuration, unit, clock);
    }

    /**
     * On the next interval all consumers will receive the latest data from their respective sources.
     */
    public void setActive() {
        isActive.set(true);
    }

    /**
     * All consumers will be given idle state inputs, and data transfer will halt.
     */
    public void setIdle() {
        isActive.set(false);
        clock.createWorker().schedule(() -> idleStateSetters.forEach(Runnable::run));
    }

    /**
     * Set the state to idle and clear all mediation rules.
     */
    public void dispose() {
        setIdle();
        subscriptions.unsubscribe();
        idleStateSetters.clear();
    }

    /**
     * Add a mediation rule for data transfer between a Supplier and a Consumer.
     * Supplier values are only passed to the consumer when the DataMediation is active.
     *
     * @param supplier a source for the consumer.
     * @param consumer a destination for the supplier.
     * @param <Param> the base type for objects which can be handled by the consumer.
     * @param <Source> the supplier type used for consumer input.
     * @return this
     */
    public <Param, Source extends Param> DataMediation mediate(
        final Supplier<Source> supplier,
        final Consumer<Param> consumer
    ) {
        subscriptions.add(interval.startWith(0L)
            .filter(t -> isActive.get())
            .map(t -> supplier.get())
            .subscribe(consumer::accept, Logger::warn));
        return this;
    }

    /**
     * Add a mediation rule for data transfer between an Observable and a Consumer.
     * Observable values are only passed to the consumer when the DataMediation is active.
     * Otherwise the idle value is passed to the consumer.
     *
     * @param source a source for the consumer.
     * @param idle the default value to be passed to the consumer.
     * @param consumer a destination for the observable.
     * @param <Param> the base type for objects which can be handled by the consumer.
     * @param <Source> the observable type  used for consumer input.
     * @return this
     */
    public <Param, Source extends Param> DataMediation mediate(
        final Observable<Source> source,
        final Source idle,
        final Consumer<Param> consumer
    ) {
        idleStateSetters.add(() -> consumer.accept(idle));
        subscriptions.add(interval.startWith(0L)
            .withLatestFrom(source.startWith(idle), (t, v) -> v)
            .filter(v -> isActive.get())
            .subscribe(consumer::accept, Logger::warn));
        return this;
    }

    /**
     * Add a mediation rule for data transfer between two observables and a two input Consumer.
     * Observable values are only passed to the consumer when the DataMediation is active.
     * Otherwise the idle values are passed to the consumer.
     *
     * @param source1 a source for the first argument to the consumer.
     * @param idle1 the default value to be passed as the first argument to the consumer.
     * @param source2 a source for the second argument to the consumer.
     * @param idle2 the default value to be passed to the second argument to the consumer.
     * @param consumer a destination for the observables.
     * @param <Param1> the base type for objects which can be handled by the first argument of the consumer.
     * @param <Source1> the observable type used for the first argument of the consumer input.
     * @param <Param2> the base type for objects which can be handled by the second argument of the consumer.
     * @param <Source2> the observable type  used for the second argument of the consumer input.
     * @return this
     */
    public <Param1, Source1 extends Param1, Param2, Source2 extends Param2> DataMediation mediate(
        final Observable<Source1> source1,
        final Source1 idle1,
        final Observable<Source2> source2,
        final Source2 idle2,
        final BiConsumer<Param1, Param2> consumer
    ) {
        idleStateSetters.add(() -> consumer.accept(idle1, idle2));
        subscriptions.add(interval.startWith(0L)
            .withLatestFrom(
                Observable.combineLatest(source1.startWith(idle1), source2.startWith(idle2), Pair::new),
                (t, v) -> v)
            .filter(v -> isActive.get())
            .subscribe(p -> consumer.accept(p.getFirst(), p.getSecond()), Logger::warn));
        return this;
    }
}
