package com.easternedgerobotics.rov.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;
import java.util.stream.LongStream;

@SuppressWarnings("checkstyle:MagicNumber")
public final class RingBufferTest {
    @Test
    public final void addDoesAddElementToBuffer() {
        final RingBuffer buffer = new RingBuffer(4);
        buffer.add(5);
        Assert.assertArrayEquals(LongStream.of(5).toArray(), buffer.apply(Function.identity()).toArray());
    }

    @Test
    public final void addToFullBufferDoesWrapAround() {
        final RingBuffer buffer = new RingBuffer(4);
        buffer.add(1);
        buffer.add(2);
        buffer.add(3);
        buffer.add(4);
        buffer.add(5);
        Assert.assertArrayEquals(LongStream.of(2, 3, 4, 5).toArray(), buffer.apply(Function.identity()).toArray());
    }
}
