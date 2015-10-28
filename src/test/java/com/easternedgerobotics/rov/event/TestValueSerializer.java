package com.easternedgerobotics.rov.event;

import com.easternedgerobotics.rov.event.io.Serializer;

class TestValueSerializer implements Serializer {
    @Override
    public byte[] serialize(final Object object) {
        final TestValue value = (TestValue) object;
        return new byte[] {value.getValue()};
    }

    @Override
    public Object deserialize(final byte[] bytes) {
        return new TestValue(bytes[0]);
    }
}
