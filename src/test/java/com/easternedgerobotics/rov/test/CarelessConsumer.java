package com.easternedgerobotics.rov.test;

import java.util.function.Consumer;

@FunctionalInterface
public interface CarelessConsumer<T> extends Consumer<T> {
    void acceptCarelessly(T elem) throws Exception;

    @Override
    default void accept(final T value) {
        try {
            acceptCarelessly(value);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
