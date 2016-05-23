package com.easternedgerobotics.rov.test;

import com.easternedgerobotics.rov.event.EventPublisher;

import rx.Observable;
import rx.observers.TestObserver;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.HashMap;
import java.util.Map;

public final class TestEventPublisher implements EventPublisher {
    private final TestScheduler scheduler;

    private final Map<Class<?>, TestSubject<?>> subjects;

    public TestEventPublisher(final TestScheduler scheduler) {
        this.scheduler = scheduler;
        this.subjects = new HashMap<>();
    }

    public final <T> TestObserver<T> testObserver(final Class<T> clazz) {
        return new TestObserver<>(subject(clazz));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void emit(final Object value) {
        ((TestSubject<Object>) subject(value.getClass())).onNext(value);
    }

    @Override
    public final <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return subject(clazz);
    }

    @Override
    public final void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void await() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private <T> TestSubject<T> subject(final Class<T> clazz) {
        return (TestSubject<T>) subjects.computeIfAbsent(clazz, k -> TestSubject.create(scheduler));
    }
}
