package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.joystick.LogitechExtremeJoystick;
import com.easternedgerobotics.rov.value.MotionValue;

import net.java.games.input.Component;
import net.java.games.input.Event;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogitechExtremeJoystickTest {
    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void axesDoesTransformAxisEventsIntoJoystickAxesValues() {
        final List<Component> joystickButtons = joystickButtons();
        final List<Component> joystickAxes = joystickAxes(new float[][] {
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
            {0, 0, 0, 0, 0, 0},
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
        });

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        events.onNext(event(joystickAxes.get(0)), 100);
        events.onNext(event(joystickAxes.get(0)), 200);
        events.onNext(event(joystickAxes.get(0)), 300);

        final TestSubscriber<MotionValue> joystickSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(events, joystickAxes, joystickButtons);

        joystick.axes().subscribe(joystickSubscriber);

        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

        joystickSubscriber.assertNoErrors();
        joystickSubscriber.assertValueCount(3);
    }

    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void axesDoesIncludeRollWhileTriggerIsBeingHeld() {
        final List<Component> joystickButtons = joystickButtons();
        final List<Component> joystickAxes = joystickAxes(new float[][] {
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
        });

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        events.onNext(event(joystickAxes.get(0)), 100);
        events.onNext(event(joystickAxes.get(0)), 200);
        events.onNext(eventWithValue(joystickButtons.get(0), 1), 300);
        events.onNext(event(joystickAxes.get(0)), 400);
        events.onNext(event(joystickAxes.get(0)), 500);
        events.onNext(event(joystickAxes.get(0)), 600);
        events.onNext(eventWithValue(joystickButtons.get(0), 0), 700);
        events.onNext(event(joystickAxes.get(0)), 800);

        final TestSubscriber<MotionValue> joystickSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(events, joystickAxes, joystickButtons);

        joystick.axes().subscribe(joystickSubscriber);

        scheduler.advanceTimeBy(800, TimeUnit.MILLISECONDS);

        joystickSubscriber.assertNoErrors();
        joystickSubscriber.assertValueCount(6);
        joystickSubscriber.assertReceivedOnNext(Arrays.asList(
            new MotionValue(1, -1, 1, 0, 1, 0),
            new MotionValue(2, -2, 2, 0, 2, 0),
            new MotionValue(3,  0, 3, 0, 3, 3),
            new MotionValue(4,  0, 4, 0, 4, 4),
            new MotionValue(5,  0, 5, 0, 5, 5),
            new MotionValue(6, -6, 6, 0, 6, 0)
        ));
    }

    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void buttonDoesFilterOutButtonByGivenIndex() {
        final List<Component> joystickButtons = joystickButtons();
        final List<Component> joystickAxes = joystickAxes(new float[][] {
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
            {0, 0, 0, 0, 0, 0},
            {1, 2, 3, 4, 5, 6},
            {1, 2, 3, 4, 5, 6},
        });

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        events.onNext(event(joystickAxes.get(0)), 100);
        events.onNext(event(joystickAxes.get(0)), 200);
        events.onNext(eventWithValue(joystickButtons.get(0), 1f), 300);
        events.onNext(event(joystickAxes.get(0)), 400);

        final TestSubscriber<Boolean> buttonSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(events, joystickAxes, joystickButtons);

        joystick.button(1).subscribe(buttonSubscriber);

        scheduler.advanceTimeBy(400, TimeUnit.MILLISECONDS);

        buttonSubscriber.assertNoErrors();
        buttonSubscriber.assertValueCount(1);
    }

    private List<Component> joystickButtons() {
        return Collections.singletonList(new TestComponent() {
            @Override
            public Identifier getIdentifier() {
                return Identifier.Button.TRIGGER;
            }
        });
    }

    /**
     * Create the axis components for the joystick.
     *
     * <p>
     * The components returned will return the given values when asked for their poll
     * data. For example, the 0th float array given will correspond to the 0th axis'
     * poll data.
     * <p>
     * See {@see Joystick#logitechExtreme3dPro} for the order of the axes on the joystick.
     *
     * @param values an array of float arrays representing the poll data for the axes.
     * @return axis {@link Component}s returning the given data for their poll data.
     */
    private List<Component> joystickAxes(final float[][] values) {
        return Arrays.stream(values).map(this::axisWithPollValues).collect(Collectors.toList());
    }

    private Component axisWithPollValues(final float... values) {
        return new TestComponent() {
            private int callCount = 0;

            @Override
            public Identifier getIdentifier() {
                return Identifier.Axis.UNKNOWN;
            }

            @Override
            public float getPollData() {
                return values[callCount++];
            }
        };
    }

    private Event event(final Component component) {
        return eventWithValue(component, 0);
    }

    private Event eventWithValue(final Component component, final float value) {
        final Event event = new Event();
        event.set(component, value, 0);

        return event;
    }
}
