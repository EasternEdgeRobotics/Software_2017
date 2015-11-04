package com.easternedgerobotics.rov.event.io;

import org.junit.Assert;
import org.junit.Test;

class Bar {
    Bar() {

    }

    @SuppressWarnings({"checkstyle:visibilitymodifier"})
    public int x;
}

class Foo {
    Foo() {

    }

    @SuppressWarnings({"checkstyle:visibilitymodifier"})
    public int y;

    @SuppressWarnings({"checkstyle:visibilitymodifier"})
    public Bar b;
}

public class KryoSerializerTest {
    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void serializeShouldReturnBytes() {
        final Foo f = new Foo();
        final Bar b = new Bar();
        b.x = 4;
        f.y = 2;
        f.b = b;

        final Serializer serializer = new KryoSerializer();
        final byte[] bytes = serializer.serialize(f);

        Assert.assertNotNull(bytes);
    }

    @Test
    @SuppressWarnings({"checkstyle:magicnumber", "checkstyle:indentation"})
    public final void deserializeShould() {
        final byte[] bytes = {
              1,   0,  99, 111, 109,  46, 101,  97, 115, 116, 101, 114, 110,
            101, 100, 103, 101, 114, 111,  98, 111, 116, 105,  99, 115,  46,
            114, 111, 118,  46, 101, 118, 101, 110, 116,  46, 105, 111,  46,
             70, 111, -17,   1,   1,   1,  99, 111, 109,  46, 101,  97, 115,
            116, 101, 114, 110, 101, 100, 103, 101, 114, 111,  98, 111, 116,
            105,  99, 115,  46, 114, 111, 118,  46, 101, 118, 101, 110, 116,
             46, 105, 111,  46,  66,  97, -14,   1,   0,   0,   0,   4,   0,
              0,   0,   2
        };

        final Serializer serializer = new KryoSerializer();

        Assert.assertNotNull(serializer.deserialize(bytes));
    }
}
