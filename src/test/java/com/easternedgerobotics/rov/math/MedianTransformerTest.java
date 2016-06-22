package com.easternedgerobotics.rov.math;

import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class MedianTransformerTest {
    @Test
    public final void medianCalculationDoesReturnCorrectResult() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Double> numbers = TestSubject.create(scheduler);
        final TestSubscriber<Double> subscriber = new TestSubscriber<>();
        final MedianTransformer<Double> transformer = new MedianTransformer<>(3);

        transformer.call(numbers).subscribe(subscriber);

        numbers.onNext(1.0);
        scheduler.triggerActions();
        subscriber.assertValueCount(1);
        subscriber.assertValue(1.0);

        numbers.onNext(7.0);
        scheduler.triggerActions();
        subscriber.assertValueCount(2);
        subscriber.assertValues(1.0, 4.0);

        numbers.onNext(5.0);
        scheduler.triggerActions();
        subscriber.assertValueCount(3);
        subscriber.assertValues(1.0, 4.0, 5.0);

        numbers.onNext(6.0);
        scheduler.triggerActions();
        subscriber.assertValueCount(4);
        subscriber.assertValues(1.0, 4.0, 5.0, 6.0);
    }
}
