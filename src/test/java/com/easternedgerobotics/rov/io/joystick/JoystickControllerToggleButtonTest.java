package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.config.JoystickConfig;
import com.easternedgerobotics.rov.config.MockJoystickConfig;
import com.easternedgerobotics.rov.control.MotionReverser;
import com.easternedgerobotics.rov.control.SpeedRegulator;
import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoFlipValueB;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public final class JoystickControllerToggleButtonTest {
    @Parameters(name = "Joystick button #{0} should emit a value of {1}")
    public static Collection<Object[]> data() {
        final JoystickConfig config = new MockJoystickConfig();
        return Arrays.asList(new Object[][] {
            {config.cameraAVideoFlipButton(), VideoFlipValueA.class},
            {config.cameraBVideoFlipButton(), VideoFlipValueB.class},
        });
    }

    private final String name;

    private final Class clazz;

    public JoystickControllerToggleButtonTest(final String name, final Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void buttonPressDoesEmitSingleValueOfCorrectType() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber subscriber = new TestSubscriber();
        final TestSubject subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickConfig config = new MockJoystickConfig();
        final JoystickController joystickController = new JoystickController(
            eventPublisher,
            m -> m,
            MotionReverser::apply,
            SpeedRegulator::apply,
            config);

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axis(ArgumentMatchers.any()))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(name)))
            .thenReturn(subj);
        Mockito.when(joystick.toggleButton(AdditionalMatchers.not(ArgumentMatchers.matches(name))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.toggleButton(Mockito.eq(name)))
            .thenReturn(subj);

        joystickController.start(Observable.just(joystick));

        eventPublisher.valuesOfType(clazz).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(2);
        Assert.assertTrue("Emitted type is not correct", subscriber.getOnNextEvents().get(0).getClass().equals(clazz));
    }
}
