package com.easternedgerobotics.rov.event;

import com.easternedgerobotics.rov.value.ImmutableValueCompanion;
import com.easternedgerobotics.rov.value.MutableValueCompanion;

class TestValue implements ImmutableValueCompanion<TestValue>, MutableValueCompanion<TestValue> {
    private byte value;

    public TestValue(final byte v) {
        value = v;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public TestValue asImmutable() {
        return this;
    }

    @Override
    public TestValue asMutable() {
        return this;
    }
}
