package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.config.MockJoystickConfig;
import com.easternedgerobotics.rov.control.MotionReverser;
import com.easternedgerobotics.rov.control.SpeedRegulator;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.MotionValue;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"checkstyle:magicnumber"})
public class JoystickControllerAxisTest {
    private final JoystickConfig config;

    public JoystickControllerAxisTest() {
        config = new MockJoystickConfig();
    }

    private JoystickController getJoystickController(final EventPublisher eventPublisher) {
        return new JoystickController(
            eventPublisher,
            m -> m,
            MotionReverser::apply,
            SpeedRegulator::apply,
            m -> m,
            config);
    }

    @Test
    @SuppressWarnings({"checkstyle:magicnumber"})
    public final void axesDoesTransformAxisEventsIntoJoystickAxesValues() {
        final TestScheduler scheduler = new TestScheduler();
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = getJoystickController(eventPublisher);

        final List<String> axes = Arrays.asList(
            config.heaveAxis(), config.surgeAxis(), config.swayAxis(), config.yawAxis());

        final TestSubject<Float> subj = TestSubject.create(scheduler);
        final TestSubscriber<MotionValue> joystickSubscriber = new TestSubscriber<>();

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any())).thenReturn(subj);
        Mockito.when(joystick.button(ArgumentMatchers.any())).thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(ArgumentMatchers.any())).thenReturn(Observable.never());

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(MotionValue.class)
            .buffer(axes.size())
            .map(l -> l.get(axes.size() - 1))
            .subscribe(joystickSubscriber);

        scheduler.advanceTimeBy(LogitechExtremeJoystick.INITIAL_INPUT_DELAY, TimeUnit.MILLISECONDS);

        subj.onNext(1f, 100);
        subj.onNext(2f, 200);
        subj.onNext(3f, 300);

        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

        joystickSubscriber.assertNoErrors();
        joystickSubscriber.assertValueCount(3);
    }
}
