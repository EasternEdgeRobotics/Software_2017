package com.easternedgerobotics.rov.io.devices;

import java.util.AbstractList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MockI2CBus extends AbstractList<I2C> {
    private final Map<Integer, I2C> lookup = new ConcurrentHashMap<>();

    @Override
    public I2C get(final int index) {
        return lookup.computeIfAbsent(index, i -> new MockI2C());
    }

    @Override
    public int size() {
        return Byte.MAX_VALUE;
    }
}
