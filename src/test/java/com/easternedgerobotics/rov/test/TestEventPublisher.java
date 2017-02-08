package com.easternedgerobotics.rov.test;

import com.easternedgerobotics.rov.event.EventPublisher;

import rx.Observable;
import rx.schedulers.TestScheduler;
import rx.subjects.Subject;
import rx.subjects.TestSubject;

import java.util.concurrent.ConcurrentHashMap;

public final class TestEventPublisher implements EventPublisher {
    private final Subject<Object, Object> values;

    private final ConcurrentHashMap<Class, Observable> streams = new ConcurrentHashMap<>();

    public TestEventPublisher(final TestScheduler scheduler) {
        values = TestSubject.create(scheduler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void emit(final Object value) {
        values.onNext(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return (Observable<T>) streams.computeIfAbsent(clazz, k -> values.filter(k::isInstance).cast(k).share());
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
