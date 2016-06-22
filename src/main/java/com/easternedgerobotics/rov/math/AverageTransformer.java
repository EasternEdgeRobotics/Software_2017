package com.easternedgerobotics.rov.math;

import rx.Observable;
import rx.Observable.Transformer;

public final class AverageTransformer<T extends Number> implements Transformer<T, Double> {
    private final RingBuffer buffer;

    public AverageTransformer(final int sample) {
        this.buffer = new RingBuffer(sample);
    }

    @Override
    public final Observable<Double> call(final Observable<T> source) {
        return source.map(value -> {
            buffer.add(value.doubleValue());
            return buffer.apply(s -> s.summaryStatistics().getAverage());
        });
    }
}
