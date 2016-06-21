package com.easternedgerobotics.rov.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;
import java.util.stream.DoubleStream;

@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public final class RingBufferTest {
    private static final double DELTA = 0.01;

    @Test
    public final void addDoesAddElementToBuffer() {
        final RingBuffer buffer = new RingBuffer(4);
        buffer.add(5);
        Assert.assertArrayEquals(DoubleStream.of(5).toArray(), buffer.apply(Function.identity()).toArray(), DELTA);
    }

    @Test
    public final void addToFullBufferDoesWrapAround() {
        final RingBuffer buffer = new RingBuffer(4);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);
        buffer.add(5);
        Assert.assertArrayEquals(DoubleStream.of(2, 3, 4, 5).toArray(), buffer.apply(Function.identity()).toArray(), DELTA);
    }
}
