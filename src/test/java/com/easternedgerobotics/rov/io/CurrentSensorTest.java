package com.easternedgerobotics.rov.io;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:MagicNumber"})
public final class CurrentSensorTest {
    @Parameters(name = "{0}V from the ADC should be {1}A")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {0.5f, 0f},
            {1.83f, 10f},
        });
    }

    private final float voltage;

    private final float current;

    public CurrentSensorTest(final float voltage, final float current) {
        this.voltage = voltage;
        this.current = current;
    }

    @Test
    public final void read() {
        final float delta = 0.0001f;
        final CurrentSensor currentSensor = CurrentSensor.V05.apply(() -> voltage);
        Assert.assertEquals(current, currentSensor.read().getValue(), delta);
    }
}
