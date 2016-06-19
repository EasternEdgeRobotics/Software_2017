package com.easternedgerobotics.rov.math;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.LongStream.Builder;

@SuppressWarnings("WeakerAccess")
final class RingBuffer {
    private final Lock lock = new ReentrantLock();

    private final int size;

    private int mark;

    private boolean filled;

    private final long[] elements;

    public RingBuffer(final int size) {
        this.size = size;
        this.mark = -1;
        this.elements = new long[size];
    }

    public final void add(final long item) {
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
    public final <R> R apply(final Function<LongStream, R> fn) {
        lock.lock();
        try {
            if (filled) {
                final Builder builder = LongStream.builder();
                for (int i = 0; i < size; i++) {
                    builder.add(elements[(mark + 1 + i) % size]);
                }
                return fn.apply(builder.build());
            }

            return fn.apply(LongStream.of(elements).limit(mark + 1));
        } finally {
            lock.unlock();
        }
    }
}
