package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.SpeedValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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
                MotionValue.create(0, 0, 0, 0, 0, 0),
                Arrays.asList(
                    SpeedValue.create("PA", 0),
                    SpeedValue.create("SA", 0),
                    SpeedValue.create("PF", 0),
                    SpeedValue.create("SF", 0),
                    SpeedValue.create("PV", 0),
                    SpeedValue.create("SV", 0)
                )
            },
            {
                MotionValue.create(0, 0, 1, 0, 0, 0),
                Arrays.asList(
                    SpeedValue.create("PA", -1),
                    SpeedValue.create("SA",  1),
                    SpeedValue.create("PF",  1),
                    SpeedValue.create("SF", -1),
                    SpeedValue.create("PV",  0),
                    SpeedValue.create("SV",  0)
                )
            },
            {
                MotionValue.create(0, -1, 1, 0, 0, 0),
                Arrays.asList(
                    SpeedValue.create("PA", -1),
                    SpeedValue.create("SA",  0),
                    SpeedValue.create("PF",  0),
                    SpeedValue.create("SF", -1),
                    SpeedValue.create("PV",  0),
                    SpeedValue.create("SV",  0)
                )
            },
            {
                MotionValue.create(0, -1, 1, 0, 1, 0),
                Arrays.asList(
                    SpeedValue.create("PA", -0.333333f),
                    SpeedValue.create("SA",  0.333333f),
                    SpeedValue.create("PF", -0.333333f),
                    SpeedValue.create("SF", -1.000000f),
                    SpeedValue.create("PV",  0.000000f),
                    SpeedValue.create("SV",  0.000000f)
                )
            },
            {
                MotionValue.create(0, -0.5f, 0.5f, 0, 0.5f, 0),
                Arrays.asList(
                    SpeedValue.create("PA", -0.1667f),
                    SpeedValue.create("SA",  0.1667f),
                    SpeedValue.create("PF", -0.1667f),
                    SpeedValue.create("SF", -0.5000f),
                    SpeedValue.create("PV",  0.0000f),
                    SpeedValue.create("SV",  0.0000f)
                )
            },
            {
                MotionValue.create(0, 0.15f, 0.5f, 0, -0.75f, 0),
                Arrays.asList(
                    SpeedValue.create("PA", -0.5893f),
                    SpeedValue.create("SA", -0.0536f),
                    SpeedValue.create("PF",  0.7500f),
                    SpeedValue.create("SF",  0.2143f),
                    SpeedValue.create("PV",  0.0000f),
                    SpeedValue.create("SV",  0.0000f)
                )
            },
            {
                MotionValue.create(1, 0, 0, 0, 0, 0),
                Arrays.asList(
                    SpeedValue.create("PA",  0),
                    SpeedValue.create("SA",  0),
                    SpeedValue.create("PF",  0),
                    SpeedValue.create("SF",  0),
                    SpeedValue.create("PV", -1),
                    SpeedValue.create("SV",  1)
                )
            },
            {
                MotionValue.create(0.8f, 0, 0, 0, 0, -0.25f),
                Arrays.asList(
                    SpeedValue.create("PA",  0f),
                    SpeedValue.create("SA",  0f),
                    SpeedValue.create("PF",  0f),
                    SpeedValue.create("SF",  0f),
                    SpeedValue.create("PV", -0.8000f),
                    SpeedValue.create("SV",  0.4190f)
                )
            },
            {
                MotionValue.create(-0.4f, 0.3f, 1f, 0, 0.5f, -0.8f),
                Arrays.asList(
                    SpeedValue.create("PA", -0.111111f),
                    SpeedValue.create("SA",  1.000000f),
                    SpeedValue.create("PF",  0.444444f),
                    SpeedValue.create("SF", -0.666667f),
                    SpeedValue.create("PV", -0.2256f),
                    SpeedValue.create("SV", -0.8000f)
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
            SpeedValue.zero("PA"),
            SpeedValue.zero("SA"),
            SpeedValue.zero("PF"),
            SpeedValue.zero("SF"),
            SpeedValue.zero("PV"),
            SpeedValue.zero("SV")
        );

        motionPowerValues.onNext(MotionPowerValue.create(1, 1, 1, 1, 1, 1, 1));
        motionValues.onNext(motionValue);
        testScheduler.triggerActions();

        sixThrusterConfig.update();

        for (SpeedValue thrusterValue : expectedThrusterValues) {
            Mockito.verify(eventPublisher).emit(thrusterValue);
        }
    }
}
