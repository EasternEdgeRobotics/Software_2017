package com.easternedgerobotics.rov.control;

import javafx.util.Pair;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Timestamped;

public final class TwoActionButton {
    /**
     * Observes toggles in the button state.
     */
    private final Observable<Timestamped<Boolean>> toggle;

    /**
     * Observes long button clicks.
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
        toggle = Observable.zip(input.first().map(x -> !x).mergeWith(input), input, Pair::new)
            .filter(p -> p.getValue() != p.getKey()).map(Pair::getValue).observeOn(scheduler).timestamp();
    }

    /**
     * Observe short button clicks.
     *
     * @return observable
     */
    public Observable<Boolean> shortClick() {
        return Observable.zip(toggle, toggle.skip(1), Pair::new)
            .filter(p -> Math.abs(p.getKey().getTimestampMillis() - p.getValue().getTimestampMillis()) < holdDuration)
            .map(Pair::getValue).map(Timestamped::getValue);
    }

    /**
     * Observe long button clicks.
     *
     * @return observable
     */
    public Observable<Boolean> longClick() {
        return Observable.zip(toggle, toggle.skip(1), Pair::new)
            .filter(p -> Math.abs(p.getKey().getTimestampMillis() - p.getValue().getTimestampMillis()) >= holdDuration)
            .map(Pair::getValue).map(Timestamped::getValue);
    }
}
