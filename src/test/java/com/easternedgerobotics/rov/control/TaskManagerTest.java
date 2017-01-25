package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.test.CollectionAssert;

import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskManagerTest {
    private static final long TIME_STEP = 100;

    @Test
    public final void doesInitialValuesWhenStopped() {
        final TestScheduler scheduler = new TestScheduler();
        final TaskManager manager = new TaskManager(TIME_STEP, TimeUnit.MILLISECONDS, scheduler);
        final List<String> results = new ArrayList<>();
        final String sourceValue = "ON";
        final Observable<String> source = Observable.just(sourceValue);
        final String initial = "OFF";
        final Consumer<String> consumer = results::add;
        manager.manage(source, initial, consumer);
        Assert.assertEquals(0, results.size());
        manager.stop();
        scheduler.triggerActions();
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(initial, results.get(0));
    }

    @Test
    public final void doesSourceValuesWhenStarted() {
        final TestScheduler scheduler = new TestScheduler();
        final TaskManager manager = new TaskManager(TIME_STEP, TimeUnit.MILLISECONDS, scheduler);
        final List<String> results = new ArrayList<>();
        final String sourceValue = "ON";
        final Observable<String> source = Observable.just(sourceValue);
        final String initial = "OFF";
        final Consumer<String> consumer = results::add;
        manager.manage(source, initial, consumer);
        manager.start();
        scheduler.advanceTimeBy(TIME_STEP, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(sourceValue, results.get(0));
    }

    @Test
    public final void doesStopStartStop() {
        final TestScheduler scheduler = new TestScheduler();
        final TaskManager manager = new TaskManager(TIME_STEP, TimeUnit.MILLISECONDS, scheduler);
        final List<String> results = new ArrayList<>();
        final String sourceValue = "ON";
        final Observable<String> source = Observable.just(sourceValue);
        final String initial = "OFF";
        final Consumer<String> consumer = results::add;
        manager.manage(source, initial, consumer);
        Assert.assertEquals(0, results.size());
        manager.stop();
        manager.start();
        scheduler.advanceTimeBy(TIME_STEP, TimeUnit.MILLISECONDS);
        scheduler.advanceTimeBy(TIME_STEP, TimeUnit.MILLISECONDS);
        scheduler.advanceTimeBy(TIME_STEP, TimeUnit.MILLISECONDS);
        manager.stop();
        scheduler.triggerActions();
        final List<String> expected = Arrays.asList(
            initial, sourceValue, sourceValue, sourceValue, initial);
        CollectionAssert.assertItemsMatchPredicateInOrder(
            expected, results, String::equals);
    }

    @Test
    public final void doesUpdateSourceStartStop() {
        final TestScheduler scheduler = new TestScheduler();
        final TaskManager manager = new TaskManager(TIME_STEP, TimeUnit.MILLISECONDS, scheduler);
        final List<String> results = new ArrayList<>();
        final String val1 = "VAL1";
        final String va12 = "VAL2";
        final String initial = "DEFAULT";
        final List<String> expected = Arrays.asList(
            val1, va12, val1, va12, val1);
        final TestSubject<String> source = TestSubject.create(scheduler);
        final Consumer<String> consumer = results::add;
        manager.manage(source, initial, consumer);
        manager.start();
        for (final String s : expected) {
            source.onNext(s);
            scheduler.advanceTimeBy(TIME_STEP, TimeUnit.MILLISECONDS);
        }
        scheduler.triggerActions();
        CollectionAssert.assertItemsMatchPredicateInOrder(
            expected, results, String::equals);
    }
}
