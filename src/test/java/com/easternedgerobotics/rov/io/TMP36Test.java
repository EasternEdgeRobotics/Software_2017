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
public final class TMP36Test {
    @Parameters(name = "{0}V from the ADC should be {1}°")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {0.75f, 25f},
        });
    }

    private final float voltage;

    private final float temperature;

    public TMP36Test(final float voltage, final float temperature) {
        this.voltage = voltage;
        this.temperature = temperature;
    }

    @Test
    public final void read() {
        final float delta = 0.0001f;
        final TMP36 sensor = new TMP36(() -> voltage);
        Assert.assertEquals(temperature, sensor.read(), delta);
    }
}
