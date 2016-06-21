package com.easternedgerobotics.rov.math;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.DoubleStream;

@SuppressWarnings("WeakerAccess")
final class RingBuffer {
    private final Lock lock = new ReentrantLock();

    private final int size;

    private int mark;

    private boolean filled;

    private final double[] elements;

    public RingBuffer(final int size) {
        this.size = size;
        this.mark = -1;
        this.elements = new double[size];
    }

    public final void add(final double item) {
        lock.lock();
        try {
            mark = (mark + 1) % size;
            elements[mark] = item;
            filled = filled || mark == (size - 1);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    public final <R> R apply(final Function<DoubleStream, R> fn) {
        lock.lock();
        try {
            if (filled) {
                final DoubleStream.Builder builder = DoubleStream.builder();
                for (int i = 0; i < size; i++) {
                    builder.add(elements[(mark + 1 + i) % size]);
                }
                return fn.apply(builder.build());
            }

            return fn.apply(DoubleStream.of(elements).limit(mark + 1));
        } finally {
            lock.unlock();
        }
    }
}
