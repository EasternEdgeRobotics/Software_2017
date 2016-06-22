package com.easternedgerobotics.rov.math;

import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class AverageTransformerTest {
    @Test
    public final void averageCalculationDoesReturnCorrectResults() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Double> numbers = TestSubject.create(scheduler);
        final TestSubscriber<Double> subscriber = new TestSubscriber<>();
        final AverageTransformer<Double> transformer = new AverageTransformer<>(2);

        transformer.call(numbers).subscribe(subscriber);

        numbers.onNext(1.5);
        scheduler.triggerActions();
        subscriber.assertValueCount(1);
        subscriber.assertValue(1.5);

        numbers.onNext(8.5);
        scheduler.triggerActions();
        subscriber.assertValueCount(2);
        subscriber.assertValues(1.5, 5.0);

        numbers.onNext(21.5);
        scheduler.triggerActions();
        subscriber.assertValueCount(3);
        subscriber.assertValues(1.5, 5.0, 15d);

        numbers.onNext(41.5);
        scheduler.triggerActions();
        subscriber.assertValueCount(4);
        subscriber.assertValues(1.5, 5.0, 15.0, 31.5);
    }
}
