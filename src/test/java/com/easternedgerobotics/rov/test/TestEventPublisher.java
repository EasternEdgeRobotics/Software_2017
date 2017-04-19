package com.easternedgerobotics.rov.test;

import com.easternedgerobotics.rov.event.EventPublisher;

import rx.Observable;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

public final class TestEventPublisher implements EventPublisher {
    private final TestSubject<Object> subject;

    public TestEventPublisher(final TestScheduler scheduler) {
        this.subject = TestSubject.create(scheduler);
    }

    @Override
    public final void emit(final Object value) {
        subject.onNext(value);
    }

    @Override
    public final <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return subject.filter(clazz::isInstance).cast(clazz);
    }

    @Override
    public final void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void await() throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
