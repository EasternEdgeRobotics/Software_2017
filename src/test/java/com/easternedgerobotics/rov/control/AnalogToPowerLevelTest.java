package com.easternedgerobotics.rov.control;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public class AnalogToPowerLevelTest {
    @Parameterized.Parameters(name = "Input {0} => Output {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {-1.00f, 1.00f},
            {-0.75f, 1.00f},
            {-0.50f, 1.00f},
            {-0.25f, 1.00f},
            {0.00f,  1.00f},
            {0.05f,  1.00f},
            {0.10f,  0.94f},
            {0.15f,  0.89f},
            {0.20f,  0.83f},
            {0.25f,  0.78f},
            {0.30f,  0.72f},
            {0.35f,  0.67f},
            {0.40f,  0.61f},
            {0.45f,  0.56f},
            {0.50f,  0.50f},
            {0.55f,  0.45f},
            {0.60f,  0.39f},
            {0.65f,  0.34f},
            {0.70f,  0.28f},
            {0.75f,  0.23f},
            {0.80f,  0.17f},
            {0.85f,  0.11f},
            {0.90f,  0.06f},
            {0.95f,  0.01f},
            {1.00f,  0.00f},
            {1.25f,  0.00f},
            {1.50f,  0.00f},
            {1.75f,  0.00f},
            {2.00f,  0.00f},
        });
    }

    private final float input;

    private final float expected;

    public AnalogToPowerLevelTest(final Object input, final Object expected) {
        this.input = (float) input;
        this.expected = (float) expected;
    }

    @Test
    public final void analogToPowerLevel() {
        Assert.assertEquals(AnalogToPowerLevel.convert(input), expected, 0.01);
    }
}
