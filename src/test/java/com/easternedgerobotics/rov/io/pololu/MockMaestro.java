package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.MockChannel;

import org.mockito.Mockito;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

public final class MockMaestro extends AbstractList<MockChannel> {
    private static final byte NUMBER_OF_CHANNELS = 24;

    private final Map<Byte, MockChannel> channels = new HashMap<>();

    @Override
    public final MockChannel get(final int index) {
        return channels.computeIfAbsent((byte) index, k -> Mockito.mock(MockChannel.class, Mockito.RETURNS_SELF));
    }

    @Override
    public final int size() {
        return NUMBER_OF_CHANNELS;
    }
}
