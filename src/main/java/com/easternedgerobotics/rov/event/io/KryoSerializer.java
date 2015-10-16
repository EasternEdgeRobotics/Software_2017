package com.easternedgerobotics.rov.event.io;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Output;

/**
 * The KryoSerializer class serializes and deserializes Java objects
 * using <a href="https://github.com/EsotericSoftware/kryo">Kryo</a>.
 * <p>
 * This class is not thread safe because Kryo is not thread safe.
 */
public class KryoSerializer implements Serializer {
    /**
     * The Kryo instance.
     */
    private final Kryo kryo;

    public KryoSerializer() {
        kryo = new Kryo();
    }

    @Override
    public final byte[] serialize(final Object value) {
        final Output output = new FastOutput(16, 1024);
        kryo.writeClassAndObject(output, value);
        return output.toBytes();
    }

    @Override
    public final Object deserialize(final byte[] bytes) {
        return kryo.readClassAndObject(new FastInput(bytes));
    }
}
