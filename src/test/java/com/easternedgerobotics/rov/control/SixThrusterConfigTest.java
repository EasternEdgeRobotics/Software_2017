package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.test.CollectionAssert;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PortAftSpeedValue;
import com.easternedgerobotics.rov.value.PortForeSpeedValue;
import com.easternedgerobotics.rov.value.PortVertSpeedValue;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.StarboardAftSpeedValue;
import com.easternedgerobotics.rov.value.StarboardForeSpeedValue;
import com.easternedgerobotics.rov.value.StarboardVertSpeedValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
                    new PortAftSpeedValue(0),
                    new StarboardAftSpeedValue(0),
                    new PortForeSpeedValue(0),
                    new StarboardForeSpeedValue(0),
                    new PortVertSpeedValue(0),
                    new StarboardVertSpeedValue(0)
                )
            },
            {
                new MotionValue(0, 0, 1, 0, 0, 0),
                Arrays.asList(
                    new PortAftSpeedValue(1),
                    new StarboardAftSpeedValue(1),
                    new PortForeSpeedValue(-1),
                    new StarboardForeSpeedValue(-1),
                    new PortVertSpeedValue(-0),
                    new StarboardVertSpeedValue(0)
                )
            },
            {
                new MotionValue(0, -1, 1, 0, 0, 0),
                Arrays.asList(
                    new PortAftSpeedValue(1),
                    new StarboardAftSpeedValue(0),
                    new PortForeSpeedValue(0),
                    new StarboardForeSpeedValue(-1),
                    new PortVertSpeedValue(0),
                    new StarboardVertSpeedValue(0)
                )
            },
            {
                new MotionValue(0, -1, 1, 0, 1, 0),
                Arrays.asList(
                    new PortAftSpeedValue(0.333333f),
                    new StarboardAftSpeedValue(0.333333f),
                    new PortForeSpeedValue(0.333333f),
                    new StarboardForeSpeedValue(-1.000000f),
                    new PortVertSpeedValue(0.000000f),
                    new StarboardVertSpeedValue(0.000000f)
                )
            },
            {
                new MotionValue(0, -0.5f, 0.5f, 0, 0.5f, 0),
                Arrays.asList(
                    new PortAftSpeedValue(0.1667f),
                    new StarboardAftSpeedValue(0.1667f),
                    new PortForeSpeedValue(0.1667f),
                    new StarboardForeSpeedValue(-0.5000f),
                    new PortVertSpeedValue(0.0000f),
                    new StarboardVertSpeedValue(0.0000f)
                )
            },
            {
                new MotionValue(0, 0.15f, 0.5f, 0, -0.75f, 0),
                Arrays.asList(
                    new PortAftSpeedValue(0.5893f),
                    new StarboardAftSpeedValue(-0.0536f),
                    new PortForeSpeedValue(-0.7500f),
                    new StarboardForeSpeedValue(0.2143f),
                    new PortVertSpeedValue(0.0000f),
                    new StarboardVertSpeedValue(0.0000f)
                )
            },
            {
                new MotionValue(1, 0, 0, 0, 0, 0),
                Arrays.asList(
                    new PortAftSpeedValue(0),
                    new StarboardAftSpeedValue(0),
                    new PortForeSpeedValue(0),
                    new StarboardForeSpeedValue(0),
                    new PortVertSpeedValue(1),
                    new StarboardVertSpeedValue(1)
                )
            },
            {
                new MotionValue(0.8f, 0, 0, 0, 0, -0.25f),
                Arrays.asList(
                    new PortAftSpeedValue(0f),
                    new StarboardAftSpeedValue(0f),
                    new PortForeSpeedValue(0f),
                    new StarboardForeSpeedValue(0f),
                    new PortVertSpeedValue(0.8000f),
                    new StarboardVertSpeedValue(0.4190f)
                )
            },
            {
                new MotionValue(-0.4f, 0.3f, 1f, 0, 0.5f, -0.8f),
                Arrays.asList(
                    new PortAftSpeedValue(0.111111f),
                    new StarboardAftSpeedValue(1.000000f),
                    new PortForeSpeedValue(-0.444444f),
                    new StarboardForeSpeedValue(-0.666667f),
                    new PortVertSpeedValue(0.2256f),
                    new StarboardVertSpeedValue(-0.8000f)
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
        final SixThrusterConfig sixThrusterConfig = new SixThrusterConfig();
        final List<SpeedValue> computedValues = Arrays.asList(
            sixThrusterConfig.update(motionValue, new MotionPowerValue(1, 1, 1, 1, 1, 1, 1)));
        CollectionAssert.assertItemsMatchPredicateInOrder(computedValues, expectedThrusterValues,
            (a, b) -> a.getClass().equals(b.getClass()) && Math.abs(a.getSpeed() - b.getSpeed()) <= 0.0001);
    }
}
