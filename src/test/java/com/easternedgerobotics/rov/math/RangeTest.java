package com.easternedgerobotics.rov.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.DoubleFunction;

@SuppressWarnings({"checkstyle:magicnumber"})
public class RangeTest {
    @Test
    public final void mapZeroReturnsZero() {
        final DoubleFunction<Double> fn = Range.map(new Range(-1, 1), new Range(-10, 10));
        Assert.assertEquals(0, fn.apply(0), 0);
    }

    @Test
    public final void mapLowEnd() {
        final DoubleFunction<Double> fn = Range.map(new Range(-1, 1), new Range(-10, 10));
        Assert.assertEquals(-10, fn.apply(-1), 0);
    }

    @Test
    public final void mapHighEnd() {
        final DoubleFunction<Double> fn = Range.map(new Range(-1, 1), new Range(-10, 10));
        Assert.assertEquals(10, fn.apply(1), 0);
    }

    @Test
    public final void mapMidRangeStart() {
        final DoubleFunction<Double> fn = Range.map(new Range(-1, 1), new Range(-10, 10));
        Assert.assertEquals(-5, fn.apply(-0.5), 0);
    }

    @Test
    public final void mapMidRangeEnd() {
        final DoubleFunction<Double> fn = Range.map(new Range(-1, 1), new Range(-10, 10));
        Assert.assertEquals(5, fn.apply(0.5), 0);
    }
}
