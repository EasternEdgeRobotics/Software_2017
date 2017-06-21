package com.easternedgerobotics.rov.io.rpi;

import com.easternedgerobotics.rov.io.devices.Light;

import java.util.AbstractList;
import java.util.RandomAccess;

public final class RPiGPIOLightProvider extends AbstractList<Light> implements RandomAccess {
    private static final int PIN_COUNT = 32;

    @Override
    public Light get(final int index) {
        if (index > PIN_COUNT) {
            throw new IndexOutOfBoundsException(String.format("This device has only %d gpio pins", PIN_COUNT));
        }
        return new RPiGPIOLight(index);
    }

    @Override
    public int size() {
        return PIN_COUNT;
    }
}
