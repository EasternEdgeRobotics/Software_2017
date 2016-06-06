package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.test.CollectionAssert;

import javafx.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import rx.schedulers.TestScheduler;
import rx.subjects.ReplaySubject;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class TwoActionButtonTest {
    private static final int LONG_CLICK_DURATION = 5;

    private static final int LONG_CLICK_DURATION_EXCEEDED = 10;

    @Parameterized.Parameters(name = "{0} => short {1}, long {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                Arrays.asList(
                    new Pair<>(false, 0),
                    new Pair<>(true, 0),
                    new Pair<>(false, 0),
                    new Pair<>(true, 0),
                    new Pair<>(false, 0)
                ),
                Arrays.asList(true, false, true, false),
                Collections.emptyList(),
            },
            {
                Arrays.asList(
                    new Pair<>(false, 0),
                    new Pair<>(true, 0),
                    new Pair<>(true, 0),
                    new Pair<>(false, 0),
                    new Pair<>(false, 0),
                    new Pair<>(false, 0),
                    new Pair<>(true, 0),
                    new Pair<>(false, 0)
                ),
                Arrays.asList(true, false, true, false),
                Collections.emptyList(),
            },
            {
                Arrays.asList(
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED)
                ),
                Collections.emptyList(),
                Arrays.asList(true, false, true, false),
            },
            {
                Arrays.asList(
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED)
                ),
                Collections.emptyList(),
                Arrays.asList(true, false, true, false),
            },
            {
                Arrays.asList(
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, 0),
                    new Pair<>(false, 0),
                    new Pair<>(true, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED)
                ),
                Arrays.asList(true, false),
                Arrays.asList(true, false),
            },
            {
                Arrays.asList(
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, 0),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(true, 0),
                    new Pair<>(true, 0),
                    new Pair<>(true, 0),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED),
                    new Pair<>(false, LONG_CLICK_DURATION_EXCEEDED)
                ),
                Arrays.asList(true, true),
                Arrays.asList(false, false),
            },
        });
    }

    private final List<Pair<Boolean, Integer>> input;

    private final List<Boolean> expectedShort;

    private final List<Boolean> expectedLong;

    public TwoActionButtonTest(
        final List<Pair<Boolean, Integer>> input,
        final List<Boolean> expectedShort,
        final List<Boolean> expectedLong
    )  {
        this.input = input;
        this.expectedShort = expectedShort;
        this.expectedLong = expectedLong;
    }

    @Test
    public final void buttonMatchesClickInputs() {
        final TestScheduler testScheduler = new TestScheduler();
        final TestSubject<Boolean> buttonValues = TestSubject.create(testScheduler);
        final TwoActionButton button = new TwoActionButton(buttonValues, LONG_CLICK_DURATION, testScheduler);
        final ReplaySubject<Boolean> shortClicks = ReplaySubject.create();
        final ReplaySubject<Boolean> longClicks = ReplaySubject.create();
        button.shortClick().subscribe(shortClicks::onNext);
        button.longClick().subscribe(longClicks::onNext);

        for (Pair<Boolean, Integer> pair : input) {
            testScheduler.advanceTimeBy(pair.getValue(), TimeUnit.MILLISECONDS);
            buttonValues.onNext(pair.getKey());
        }

        testScheduler.advanceTimeBy(LONG_CLICK_DURATION, TimeUnit.MILLISECONDS);
        shortClicks.onCompleted();
        longClicks.onCompleted();

        CollectionAssert.assertItemsMatchPredicateInOrder(
            shortClicks.toList().toBlocking().single(), expectedShort, (a, b) -> a == b);

        CollectionAssert.assertItemsMatchPredicateInOrder(
            longClicks.toList().toBlocking().single(), expectedLong, (a, b) -> a == b);
    }
}
