package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.ADC;
import com.easternedgerobotics.rov.io.devices.PWM;

import com.pi4j.io.serial.Serial;

import java.util.AbstractList;
import java.util.RandomAccess;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Maestro<T extends ADC & PWM> extends AbstractList<T> implements RandomAccess {
    private static final byte NUMBER_OF_CHANNELS = 24;

    private final Serial serial;

    private final Lock lock;

    private final byte device;

    public Maestro(final Serial serial, final byte device) {
        this.serial = serial;
        this.lock = new ReentrantLock();
        this.device = device;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T get(final int index) {
        if (index > NUMBER_OF_CHANNELS) {
            throw new IndexOutOfBoundsException(String.format("This device has only %d channels", NUMBER_OF_CHANNELS));
        }

        final byte channel = (byte) index;
        return (T) new MaestroChannel(serial, lock, device, channel);
    }

    @Override
    public final int size() {
        return NUMBER_OF_CHANNELS;
    }
}
