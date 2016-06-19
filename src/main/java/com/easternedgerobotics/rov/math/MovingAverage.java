package com.easternedgerobotics.rov.math;

import rx.Observable;

public final class MovingAverage {
    public static Observable<Float> from(final Observable<Float> source, final int n) {
        final RingBuffer buffer = new RingBuffer(n);
        return source.map(value -> {
            buffer.add(value.longValue());
            return buffer.apply(s -> (float) s.summaryStatistics().getAverage());
        });
    }

    private MovingAverage() {
        // ???
    }
}
