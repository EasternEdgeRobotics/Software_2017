package com.easternedgerobotics.rov.math;

import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

@SuppressWarnings("checkstyle:MagicNumber")
public final class MovingAverageTest {
    @Test
    public final void averageCalculationDoesReturnTheCorrectResult() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Float> numbers = TestSubject.create(scheduler);
        final TestSubscriber<Float> subscriber = new TestSubscriber<>();
        MovingAverage.from(numbers, 2).subscribe(subscriber);

        numbers.onNext(1.5f);
        scheduler.triggerActions();
        subscriber.assertValueCount(1);
        subscriber.assertValue(1.5f);

        numbers.onNext(8.5f);
        scheduler.triggerActions();
        subscriber.assertValueCount(2);
        subscriber.assertValues(1.5f, 8.5f);

        numbers.onNext(21.5f);
        scheduler.triggerActions();
        subscriber.assertValueCount(3);
        subscriber.assertValues(1.5f, 8.5f, 15f);

        numbers.onNext(41.5f);
        scheduler.triggerActions();
        subscriber.assertValueCount(4);
        subscriber.assertValues(1f, 5f, 14.5f, 30f);
    }
}
