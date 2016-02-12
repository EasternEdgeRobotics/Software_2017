package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.ThrusterValue;

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
                    ThrusterValue.create("PA", 0),
                    ThrusterValue.create("SA", 0),
                    ThrusterValue.create("PF", 0),
                    ThrusterValue.create("SF", 0),
                    ThrusterValue.create("PV", 0),
                    ThrusterValue.create("SV", 0)
                )
            },
            {
                MotionValue.create(0, 0, 1, 0, 0, 0),
                Arrays.asList(
                    ThrusterValue.create("PA",  1),
                    ThrusterValue.create("SA", -1),
                    ThrusterValue.create("PF", -1),
                    ThrusterValue.create("SF",  1),
                    ThrusterValue.create("PV",  0),
                    ThrusterValue.create("SV",  0)
                )
            },
            {
                MotionValue.create(0, -1, 1, 0, 0, 0),
                Arrays.asList(
                    ThrusterValue.create("PA", 1),
                    ThrusterValue.create("SA", 0),
                    ThrusterValue.create("PF", 0),
                    ThrusterValue.create("SF", 1),
                    ThrusterValue.create("PV", 0),
                    ThrusterValue.create("SV", 0)
                )
            },
            {
                MotionValue.create(0, -1, 1, 0, 1, 0),
                Arrays.asList(
                    ThrusterValue.create("PA",  0.333333f),
                    ThrusterValue.create("SA", -0.333333f),
                    ThrusterValue.create("PF",  0.333333f),
                    ThrusterValue.create("SF",  1.000000f),
                    ThrusterValue.create("PV",  0.000000f),
                    ThrusterValue.create("SV",  0.000000f)
                )
            },
            {
                MotionValue.create(0, -0.5f, 0.5f, 0, 0.5f, 0),
                Arrays.asList(
                    ThrusterValue.create("PA",  0.33333f),
                    ThrusterValue.create("SA", -0.33333f),
                    ThrusterValue.create("PF",  0.33333f),
                    ThrusterValue.create("SF",  1.00000f),
                    ThrusterValue.create("PV",  0.00000f),
                    ThrusterValue.create("SV",  0.00000f)
                )
            },
            {
                MotionValue.create(0, 0.15f, 0.5f, 0, -0.75f, 0),
                Arrays.asList(
                    ThrusterValue.create("PA",  0.7857f),
                    ThrusterValue.create("SA",  0.0714f),
                    ThrusterValue.create("PF", -1.0000f),
                    ThrusterValue.create("SF", -0.2857f),
                    ThrusterValue.create("PV",  0.0000f),
                    ThrusterValue.create("SV",  0.0000f)
                )
            },
            {
                MotionValue.create(1, 0, 0, 0, 0, 0),
                Arrays.asList(
                    ThrusterValue.create("PA",  0),
                    ThrusterValue.create("SA",  0),
                    ThrusterValue.create("PF",  0),
                    ThrusterValue.create("SF",  0),
                    ThrusterValue.create("PV",  1),
                    ThrusterValue.create("SV", -1)
                )
            },
            {
                MotionValue.create(0.8f, 0, 0, 0, 0, -0.25f),
                Arrays.asList(
                    ThrusterValue.create("PA",  0f),
                    ThrusterValue.create("SA",  0f),
                    ThrusterValue.create("PF",  0f),
                    ThrusterValue.create("SF",  0f),
                    ThrusterValue.create("PV",  1f),
                    ThrusterValue.create("SV", -0.5238f)
                )
            },
            {
                MotionValue.create(-0.4f, 0.3f, 1f, 0, 0.5f, -0.8f),
                Arrays.asList(
                    ThrusterValue.create("PA",  0.111111f),
                    ThrusterValue.create("SA", -1.000000f),
                    ThrusterValue.create("PF", -0.444444f),
                    ThrusterValue.create("SF",  0.666667f),
                    ThrusterValue.create("PV",  0.282000f),
                    ThrusterValue.create("SV",  1.000000f)
                )
            }
        });
    }

    final MotionValue motionValue;

    final List<ThrusterValue> expectedThrusterValues;

    public SixThrusterConfigTest(final MotionValue motionValue, final List<ThrusterValue> expectedThrusterValues) {
        this.motionValue = motionValue;
        this.expectedThrusterValues = expectedThrusterValues;
    }

    @Test
    public final void updateSixThrustersWithMotionAndFullPowerDoesEmitCorrectThrusterValue() {
        final EventPublisher eventPublisher = Mockito.mock(EventPublisher.class);
        final TestScheduler testScheduler = new TestScheduler();
        final TestSubject<MotionValue> motionValues = TestSubject.create(testScheduler);
        final TestSubject<MotionPowerValue> motionPowerValues = TestSubject.create(testScheduler);

        Mockito.when(eventPublisher.valuesOfType(ThrusterValue.class)).thenReturn(Observable.empty());
        Mockito.when(eventPublisher.valuesOfType(MotionValue.class)).thenReturn(motionValues);
        Mockito.when(eventPublisher.valuesOfType(MotionPowerValue.class)).thenReturn(motionPowerValues);

        final SixThrusterConfig sixThrusterConfig = new SixThrusterConfig(
            eventPublisher,
            ThrusterValue.create("PA"),
            ThrusterValue.create("SA"),
            ThrusterValue.create("PF"),
            ThrusterValue.create("SF"),
            ThrusterValue.create("PV"),
            ThrusterValue.create("SV")
        );

        motionPowerValues.onNext(MotionPowerValue.create(1, 1, 1, 1, 1, 1, 1));
        motionValues.onNext(motionValue);
        testScheduler.triggerActions();

        sixThrusterConfig.update();

        for (ThrusterValue thrusterValue : expectedThrusterValues) {
            Mockito.verify(eventPublisher).emit(thrusterValue);
        }
    }
}
