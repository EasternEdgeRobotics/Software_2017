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
            {-1.00f, 100},
            {-0.75f, 100},
            {-0.50f, 100},
            {-0.25f, 100},
            {0.00f,  100},
            {0.05f,  100},
            {0.10f,   94},
            {0.15f,   89},
            {0.20f,   83},
            {0.25f,   78},
            {0.30f,   72},
            {0.35f,   67},
            {0.40f,   61},
            {0.45f,   56},
            {0.50f,   50},
            {0.55f,   45},
            {0.60f,   39},
            {0.65f,   34},
            {0.70f,   28},
            {0.75f,   23},
            {0.80f,   17},
            {0.85f,   11},
            {0.90f,    6},
            {0.95f,    1},
            {1.00f,    0},
            {1.25f,    0},
            {1.50f,    0},
            {1.75f,    0},
            {2.00f,    0},
        });
    }

    private final float input;

    private final int expected;

    public AnalogToPowerLevelTest(final Object input, final Object expected) {
        this.input = (Float) input;
        this.expected = (Integer) expected;
    }

    @Test
    public final void analogToPowerLevel() {
        Assert.assertEquals(AnalogToPowerLevel.convert(input), expected);
    }
}
