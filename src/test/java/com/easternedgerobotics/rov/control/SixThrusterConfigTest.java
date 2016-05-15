package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.test.CollectionAssert;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.SpeedValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import rx.Observable;
import rx.schedulers.TestScheduler;
import rx.subjects.TestSubject;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public class SixThrusterConfigTest {
    @Parameters(name = "{0} => {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                new MotionValue(0, 0, 0, 0, 0, 0),
                Arrays.asList(
                    new SpeedValue("PA", 0),
                    new SpeedValue("SA", 0),
                    new SpeedValue("PF", 0),
                    new SpeedValue("SF", 0),
                    new SpeedValue("PV", 0),
                    new SpeedValue("SV", 0)
                )
            },
            {
                new MotionValue(0, 0, 1, 0, 0, 0),
                Arrays.asList(
                    new SpeedValue("PA", -1),
                    new SpeedValue("SA",  1),
                    new SpeedValue("PF",  1),
                    new SpeedValue("SF", -1),
                    new SpeedValue("PV",  0),
                    new SpeedValue("SV",  0)
                )
            },
            {
                new MotionValue(0, -1, 1, 0, 0, 0),
                Arrays.asList(
                    new SpeedValue("PA", -1),
                    new SpeedValue("SA",  0),
                    new SpeedValue("PF",  0),
                    new SpeedValue("SF", -1),
                    new SpeedValue("PV",  0),
                    new SpeedValue("SV",  0)
                )
            },
            {
                new MotionValue(0, -1, 1, 0, 1, 0),
                Arrays.asList(
                    new SpeedValue("PA", -0.333333f),
                    new SpeedValue("SA",  0.333333f),
                    new SpeedValue("PF", -0.333333f),
                    new SpeedValue("SF", -1.000000f),
                    new SpeedValue("PV",  0.000000f),
                    new SpeedValue("SV",  0.000000f)
                )
            },
            {
                new MotionValue(0, -0.5f, 0.5f, 0, 0.5f, 0),
                Arrays.asList(
                    new SpeedValue("PA", -0.1667f),
                    new SpeedValue("SA",  0.1667f),
                    new SpeedValue("PF", -0.1667f),
                    new SpeedValue("SF", -0.5000f),
                    new SpeedValue("PV",  0.0000f),
                    new SpeedValue("SV",  0.0000f)
                )
            },
            {
                new MotionValue(0, 0.15f, 0.5f, 0, -0.75f, 0),
                Arrays.asList(
                    new SpeedValue("PA", -0.5893f),
                    new SpeedValue("SA", -0.0536f),
                    new SpeedValue("PF",  0.7500f),
                    new SpeedValue("SF",  0.2143f),
                    new SpeedValue("PV",  0.0000f),
                    new SpeedValue("SV",  0.0000f)
                )
            },
            {
                new MotionValue(1, 0, 0, 0, 0, 0),
                Arrays.asList(
                    new SpeedValue("PA",  0),
                    new SpeedValue("SA",  0),
                    new SpeedValue("PF",  0),
                    new SpeedValue("SF",  0),
                    new SpeedValue("PV", -1),
                    new SpeedValue("SV",  1)
                )
            },
            {
                new MotionValue(0.8f, 0, 0, 0, 0, -0.25f),
                Arrays.asList(
                    new SpeedValue("PA",  0f),
                    new SpeedValue("SA",  0f),
                    new SpeedValue("PF",  0f),
                    new SpeedValue("SF",  0f),
                    new SpeedValue("PV", -0.8000f),
                    new SpeedValue("SV",  0.4190f)
                )
            },
            {
                new MotionValue(-0.4f, 0.3f, 1f, 0, 0.5f, -0.8f),
                Arrays.asList(
                    new SpeedValue("PA", -0.111111f),
                    new SpeedValue("SA",  1.000000f),
                    new SpeedValue("PF",  0.444444f),
                    new SpeedValue("SF", -0.666667f),
                    new SpeedValue("PV", -0.2256f),
                    new SpeedValue("SV", -0.8000f)
                )
            }
        });
    }

    final MotionValue motionValue;

    final List<SpeedValue> expectedThrusterValues;

    public SixThrusterConfigTest(final MotionValue motionValue, final List<SpeedValue> expectedThrusterValues) {
        this.motionValue = motionValue;
        this.expectedThrusterValues = expectedThrusterValues;
    }

    @Test
    public final void updateSixThrustersWithMotionAndFullPowerDoesEmitCorrectThrusterValue() {
        final EventPublisher eventPublisher = Mockito.mock(EventPublisher.class);
        final TestScheduler testScheduler = new TestScheduler();
        final TestSubject<MotionValue> motionValues = TestSubject.create(testScheduler);
        final TestSubject<MotionPowerValue> motionPowerValues = TestSubject.create(testScheduler);

        Mockito.when(eventPublisher.valuesOfType(SpeedValue.class)).thenReturn(Observable.empty());
        Mockito.when(eventPublisher.valuesOfType(MotionValue.class)).thenReturn(motionValues);
        Mockito.when(eventPublisher.valuesOfType(MotionPowerValue.class)).thenReturn(motionPowerValues);

        final SixThrusterConfig sixThrusterConfig = new SixThrusterConfig(
            eventPublisher,
            new SpeedValue("PA"),
            new SpeedValue("SA"),
            new SpeedValue("PF"),
            new SpeedValue("SF"),
            new SpeedValue("PV"),
            new SpeedValue("SV")
        );

        motionPowerValues.onNext(new MotionPowerValue(1, 1, 1, 1, 1, 1, 1));
        motionValues.onNext(motionValue);
        testScheduler.triggerActions();

        sixThrusterConfig.update();

        final ArgumentCaptor<SpeedValue> captor = ArgumentCaptor.forClass(SpeedValue.class);
        Mockito.verify(eventPublisher, Mockito.times(6)).emit(captor.capture());
        CollectionAssert.assertItemsMatchPredicateInOrder(
            captor.getAllValues(), expectedThrusterValues, (a, b) ->
                a.getName().equals(b.getName()) && Math.abs(a.getSpeed() - b.getSpeed()) <= 0.0001);
    }
}
