package com.easternedgerobotics.rov.control;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Timestamped;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class TwoActionButton {
    /**
     * Observe state toggles in an Observable boolean.
     */
    final Observable<Timestamped<Boolean>> toggle;

    /**
     * Observes short button clicks.
     */
    private final long holdDuration;

    /**
     * Observes a boolean input to create a notice short click and long click events.
     *
     * @param input the button source.
     * @param holdDuration duration held down to be considered a long click.
     * @param scheduler the scheduler to observe on.
     */
    public TwoActionButton(final Observable<Boolean> input, final long holdDuration, final Scheduler scheduler) {
        this.holdDuration = holdDuration;
        final AtomicBoolean firstToggle = new AtomicBoolean(true);
        final AtomicBoolean lastValue = new AtomicBoolean();
        toggle = input.observeOn(scheduler).timestamp(scheduler)
            .filter(t -> t.getValue() != lastValue.getAndSet(t.getValue()) || firstToggle.getAndSet(false)).share();
    }

    /**
     * Observe short button clicks.
     *
     * @return observable
     */
    public Observable<Boolean> shortClick() {
        final AtomicLong lastTime = new AtomicLong(-1);
        return toggle.filter(t -> {
            final long last = lastTime.getAndSet(t.getTimestampMillis());
            return last != -1 && t.getTimestampMillis() - last < holdDuration;
        }).map(Timestamped::getValue);
    }

    /**
     * Observe long button clicks.
     *
     * @return observable
     */
    public Observable<Boolean> longClick() {
        final AtomicLong lastTime = new AtomicLong(-1);
        return toggle.filter(t -> {
            final long last = lastTime.getAndSet(t.getTimestampMillis());
            return last != -1 && t.getTimestampMillis() - last >= holdDuration;
        }).map(Timestamped::getValue);
    }
}
