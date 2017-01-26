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

public final class TaskManager {
    /**
     * The scheduler for running tasks managed by a TaskManager instance.
     */
    private final Scheduler clock;

    /**
     * The base observable for scheduled events.
     */
    private final Observable<Long> interval;

    /**
     * Runnables which force the managed tasks to update initial state values.
     */
    private final List<Runnable> initialStateSetters = new ArrayList<>();

    /**
     * Aggregate of all subscriptions created by this class.
     */
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * The state of the task manager.
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Create a task manager which runs operations at the supplied rate when the instance is started.
     * If a stop is requested, all tasks will default to thier supplied initial states.
     *
     * @param sleepDuration wait x units between tasks.
     * @param unit the unit that sleepDuration waits.
     * @param clock the scheduler used for performing tasks.
     */
    public TaskManager(final long sleepDuration, final TimeUnit unit, final Scheduler clock) {
        this.clock = clock;
        this.interval = Observable.interval(sleepDuration, unit, clock);
    }

    /**
     * On the next interval all operations will perform normal actions.
     */
    public void start() {
        isStarted.set(true);
    }

    /**
     * Normal actions will halt, and tasks will default to thier supplied initial states.
     */
    public void stop() {
        isStarted.set(false);
        clock.createWorker().schedule(() -> initialStateSetters.forEach(Runnable::run));
    }

    /**
     * Stop all tasks and prevent further updates.
     */
    public void dispose() {
        stop();
        subscriptions.unsubscribe();
        initialStateSetters.clear();
    }

    /**
     * Manage the transactions between a Supplier and a Consumer.
     * Supplier values are only passed to the consumer when the TaskManager is started.
     *
     * @param supplier a source for the consumer.
     * @param consumer a sink for the supplier.
     * @param <Param> the base type for objects which can be handled by the consumer.
     * @param <Source> the supplier type used for consumer input.
     * @return this
     */
    public <Param, Source extends Param> TaskManager manage(
        final Supplier<Source> supplier,
        final Consumer<Param> consumer
    ) {
        subscriptions.add(interval.startWith(0L)
            .filter(t -> isStarted.get())
            .map(t -> supplier.get())
            .subscribe(consumer::accept, Logger::warn));
        return this;
    }

    /**
     * Manage the transactions between an Observable and a Consumer.
     * Observable values are only passed to the consumer when the TaskManager is started.
     * Otherwise the initial value is passed to the consumer.
     *
     * @param source a source for the consumer.
     * @param initial the default value to be passed to the consumer.
     * @param consumer a sink for the observable.
     * @param <Param> the base type for objects which can be handled by the consumer.
     * @param <Source> the observable type  used for consumer input.
     * @return this
     */
    public <Param, Source extends Param> TaskManager manage(
        final Observable<Source> source,
        final Source initial,
        final Consumer<Param> consumer
    ) {
        initialStateSetters.add(() -> consumer.accept(initial));
        subscriptions.add(interval.startWith(0L)
            .withLatestFrom(source.startWith(initial), (t, v) -> v)
            .filter(v -> isStarted.get())
            .subscribe(consumer::accept, Logger::warn));
        return this;
    }

    /**
     * Manage the transactions between two observables and a 2 input Consumer.
     * Observable values are only passed to the consumer when the TaskManager is started.
     * Otherwise the initial values are passed to the consumer.
     *
     * @param source1 a source for the first argument to the consumer.
     * @param initial1 the default value to be passed as the first argument to the consumer.
     * @param source2 a source for the second argument to the consumer.
     * @param initial2 the default value to be passed to the second argument to the consumer.
     * @param consumer a sink for the observables.
     * @param <Param1> the base type for objects which can be handled by the first argument of the consumer.
     * @param <Source1> the observable type used for the first argument of the consumer input.
     * @param <Param2> the base type for objects which can be handled by the second argument of the consumer.
     * @param <Source2> the observable type  used for the second argument of the consumer input.
     * @return this
     */
    public <Param1, Source1 extends Param1, Param2, Source2 extends Param2> TaskManager manage(
        final Observable<Source1> source1, final Source1 initial1,
        final Observable<Source2> source2, final Source2 initial2,
        final BiConsumer<Param1, Param2> consumer
    ) {
        initialStateSetters.add(() -> consumer.accept(initial1, initial2));
        subscriptions.add(interval.startWith(0L)
            .withLatestFrom(
                Observable.combineLatest(source1.startWith(initial1), source2.startWith(initial2), Pair::new),
                (t, v) -> v)
            .filter(v -> isStarted.get())
            .subscribe(p -> consumer.accept(p.getFirst(), p.getSecond()), Logger::warn));
        return this;
    }
}
