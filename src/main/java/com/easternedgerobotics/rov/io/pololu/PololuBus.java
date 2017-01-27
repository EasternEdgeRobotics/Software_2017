package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.I2C;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.AbstractList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class PololuBus extends AbstractList<I2C> {
    /**
     * Maximum address value in i2c.
     */
    static final byte MAX_ADDRESS = 127;

    /**
     * Cache the i2c devices for further lookups.
     */
    private final Map<Integer, I2C> lookup = new ConcurrentHashMap<>();

    /**
     * The physical bus.
     */
    private final I2CBus bus;

    /**
     * Make i2c devices thread safe.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Create a Pololu i2c bus with this channel.
     * Returns empty channels if device does not exits or
     * the device cannot be found.
     *
     * @param channel the bus id.
     */
    public PololuBus(final int channel) {
        I2CBus bus;
        try {
            bus = I2CFactory.getInstance(channel);
        } catch (final IOException e) {
            bus = null;
        }
        this.bus = bus;
    }

    @Override
    public I2C get(final int index) {
        return lookup.computeIfAbsent(index, i -> {
            if (bus != null) {
                try {
                    return new PololuI2C(bus.getDevice(i), lock);
                } catch (final IOException e) {
                    Logger.warn(e);
                }
            }
            return new I2C() { };
        });
    }

    @Override
    public int size() {
        return MAX_ADDRESS;
    }
}
