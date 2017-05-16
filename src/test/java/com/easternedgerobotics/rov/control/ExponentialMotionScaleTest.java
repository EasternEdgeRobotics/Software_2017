package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public class ExponentialMotionScaleTest {
    @Parameterized.Parameters(name = "Input {0} => Output {1}")
    public static Iterable<Float[]> data() {
        return Arrays.asList(new Float[][] {
            {-0.00f,        -0f},
            {-0.05f, -0.029839f},
            {-0.10f, -0.061207f},
            {-0.15f, -0.094184f},
            {-0.20f, -0.128851f},
            {-0.25f, -0.165296f},
            {-0.30f, -0.203610f},
            {-0.35f, -0.243888f},
            {-0.40f, -0.286231f},
            {-0.45f, -0.330744f},
            {-0.50f, -0.377541f},
            {-0.55f, -0.426736f},
            {-0.60f, -0.478454f},
            {-0.65f, -0.532823f},
            {-0.70f, -0.589980f},
            {-0.75f, -0.650068f},
            {-0.80f, -0.713236f},
            {-0.85f, -0.779643f},
            {-0.90f, -0.849455f},
            {-0.95f, -0.922846f},
            {-1.00f,        -1f},

            {0.00f,        0f},
            {0.05f, 0.029839f},
            {0.10f, 0.061207f},
            {0.15f, 0.094184f},
            {0.20f, 0.128851f},
            {0.25f, 0.165296f},
            {0.30f, 0.203610f},
            {0.35f, 0.243888f},
            {0.40f, 0.286231f},
            {0.45f, 0.330744f},
            {0.50f, 0.377541f},
            {0.55f, 0.426736f},
            {0.60f, 0.478454f},
            {0.65f, 0.532823f},
            {0.70f, 0.589980f},
            {0.75f, 0.650068f},
            {0.80f, 0.713236f},
            {0.85f, 0.779643f},
            {0.90f, 0.849455f},
            {0.95f, 0.922846f},
            {1.00f,        1f}
        });
    }

    private final float input;

    private final float expected;

    public ExponentialMotionScaleTest(final float input, final float expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public final void exponentialScaling() {
        final MotionValue inputMotion = new MotionValue(input, 0, 0, 0, 0, 0);
        final MotionValue expectedMotion = new MotionValue(expected, 0, 0, 0, 0, 0);
        Assert.assertEquals(expectedMotion.getHeave(), ExponentialMotionScale.apply(inputMotion).getHeave(), 0.000001);
    }
}
