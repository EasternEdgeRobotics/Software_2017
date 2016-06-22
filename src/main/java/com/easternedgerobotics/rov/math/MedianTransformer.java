package com.easternedgerobotics.rov.math;

import rx.Observable;
import rx.Observable.Transformer;

public final class MedianTransformer<T extends Number> implements Transformer<T, Double> {
    private final RingBuffer buffer;

    public MedianTransformer(final int sample) {
        this.buffer = new RingBuffer(sample);
    }

    @Override
    public final Observable<Double> call(final Observable<T> source) {
        return source.map(value -> {
            buffer.add(value.doubleValue());
            return buffer.apply(s -> {
                final double[] values = s.sorted().toArray();
                if (values.length % 2 == 0) {
                    final double l = values[(values.length / 2) - 1];
                    final double r = values[(values.length / 2)];
                    return (l + r) / 2;
                } else {
                    return values[((values.length + 1) / 2) - 1];
                }
            });
        });
    }
}
