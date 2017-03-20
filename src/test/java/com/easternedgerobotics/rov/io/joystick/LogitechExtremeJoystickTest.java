package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.value.MotionValue;

import kotlin.Pair;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Event;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogitechExtremeJoystickTest {
    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void axesDoesTransformAxisEventsIntoJoystickAxesValues() {
        final List<Component> joystickButtons = joystickButtons();
        final Map<Component.Identifier, Component> axes = joystickAxes(Arrays.asList(
            new Pair<>(Axis.X, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.Y, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.RZ, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.SLIDER, new float[]{1, 2, 3, 4, 5, 6})
        ));

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        final TestSubscriber<MotionValue> joystickSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(scheduler, events, joystickButtons);

        joystick.axes().buffer(4).map(l -> l.get(3)).subscribe(joystickSubscriber);

        scheduler.advanceTimeBy(LogitechExtremeJoystick.INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS);

        for (final Component.Identifier id : Arrays.asList(Axis.X, Axis.Y, Axis.RZ, Axis.SLIDER)) {
            events.onNext(event(axes.get(id)), 100);
            events.onNext(event(axes.get(id)), 200);
            events.onNext(event(axes.get(id)), 300);
        }

        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

        joystickSubscriber.assertNoErrors();
        joystickSubscriber.assertValueCount(3);
    }

    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void axesDoesIncludeRollWhileTriggerIsBeingHeld() {
        final List<Component> joystickButtons = joystickButtons();
        final Map<Component.Identifier, Component> axes = joystickAxes(Arrays.asList(
            new Pair<>(Axis.X, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.Y, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.RZ, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.SLIDER, new float[]{1, 2, 3, 4, 5, 6})
        ));

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        final TestSubscriber<MotionValue> joystickSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(scheduler, events, joystickButtons);

        joystick.axes().buffer(4).map(l -> l.get(3)).subscribe(joystickSubscriber);

        scheduler.advanceTimeBy(LogitechExtremeJoystick.INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS);

        for (final Component.Identifier id : Arrays.asList(Axis.X, Axis.Y, Axis.RZ, Axis.SLIDER)) {
            events.onNext(event(axes.get(id)), 100);
            events.onNext(event(axes.get(id)), 200);
            events.onNext(event(axes.get(id)), 400);
            events.onNext(event(axes.get(id)), 500);
            events.onNext(event(axes.get(id)), 600);
            events.onNext(event(axes.get(id)), 800);
        }
        events.onNext(eventWithValue(joystickButtons.get(0), 1), 300);
        events.onNext(eventWithValue(joystickButtons.get(0), 0), 700);

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
        final Map<Component.Identifier, Component> axes = joystickAxes(Arrays.asList(
            new Pair<>(Axis.X, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.Y, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.RZ, new float[]{1, 2, 3, 4, 5, 6}),
            new Pair<>(Axis.SLIDER, new float[]{1, 2, 3, 4, 5, 6})
        ));

        final TestScheduler scheduler = new TestScheduler();
        final TestSubject<Event> events = TestSubject.create(scheduler);

        final TestSubscriber<Boolean> buttonSubscriber = new TestSubscriber<>();
        final LogitechExtremeJoystick joystick = new LogitechExtremeJoystick(scheduler, events, joystickButtons);

        joystick.button(1).subscribe(buttonSubscriber);

        scheduler.advanceTimeBy(LogitechExtremeJoystick.INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS);

        for (final Component.Identifier id : Arrays.asList(Axis.X, Axis.Y, Axis.RZ, Axis.SLIDER)) {
            events.onNext(event(axes.get(id)), 100);
            events.onNext(event(axes.get(id)), 200);
            events.onNext(event(axes.get(id)), 400);
        }
        events.onNext(eventWithValue(joystickButtons.get(0), 1f), 300);

        scheduler.advanceTimeBy(400, TimeUnit.MILLISECONDS);

        buttonSubscriber.assertNoErrors();
        buttonSubscriber.assertValueCount(1);
    }

    private List<Component> joystickButtons() {
        return Collections.singletonList(new TestComponent() {
            @Override
            public Identifier getIdentifier() {
                return Button.TRIGGER;
            }
        });
    }

    /**
     * Create the axis components for the joystick.
     *
     * <p>
     * The components returned will return the given values when asked for their poll
     * data. For example, the Axis.X float array given will correspond to the Axis.X axis'
     * poll data.
     * <p>
     *
     * @param axes a list of component id paired with float arrays representing the poll data for the axes.
     * @return axis {@link Component}s returning the given data for their poll data.
     */
    private Map<Component.Identifier, Component> joystickAxes(final List<Pair<Component.Identifier, float[]>> axes) {
        return axes.stream().map(p -> axisWithPollValues(p.getFirst(), p.getSecond()))
            .collect(Collectors.toMap(Component::getIdentifier, c -> c));
    }

    private Component axisWithPollValues(final Component.Identifier id, final float... values) {
        return new TestComponent() {
            private int callCount = 0;

            @Override
            public Identifier getIdentifier() {
                return id;
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
