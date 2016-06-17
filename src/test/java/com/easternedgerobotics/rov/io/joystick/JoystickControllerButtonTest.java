package com.easternedgerobotics.rov.io.joystick;

import com.easternedgerobotics.rov.test.TestEventPublisher;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoFlipValueB;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

@RunWith(Parameterized.class)
public final class JoystickControllerButtonTest {
    @Parameters(name = "Joystick button #{0} should emit a value of {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {JoystickController.CAMERA_A_VIDEO_FLIP_JOYSTICK_BUTTON, VideoFlipValueA.class},
            {JoystickController.CAMERA_B_VIDEO_FLIP_JOYSTICK_BUTTON, VideoFlipValueB.class},
        });
    }

    private final int index;

    private final Class clazz;

    public JoystickControllerButtonTest(final int index, final Class clazz) {
        this.index = index;
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void buttonPressDoesEmitSingleValueOfCorrectType() {
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber subscriber = new TestSubscriber();
        final TestSubject subj = TestSubject.create(scheduler);
        final TestEventPublisher eventPublisher = new TestEventPublisher(scheduler);
        final JoystickController joystickController = new JoystickController(eventPublisher, Function.identity());

        final Joystick joystick = Mockito.mock(Joystick.class);
        Mockito.when(joystick.axes())
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Matchers.intThat(argument -> !argument.equals(index))))
            .thenReturn(Observable.never());
        Mockito.when(joystick.button(Mockito.eq(index)))
            .thenReturn(subj);

        joystickController.onNext(joystick);

        eventPublisher.valuesOfType(clazz).subscribe(subscriber);
        subj.onNext(Joystick.BUTTON_DOWN);
        subj.onNext(Joystick.BUTTON_UP);
        scheduler.triggerActions();

        subscriber.assertValueCount(1);
        Assert.assertTrue("Emitted type is not correct", subscriber.getOnNextEvents().get(0).getClass().equals(clazz));
    }
}
