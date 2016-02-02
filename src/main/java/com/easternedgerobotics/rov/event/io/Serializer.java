package com.easternedgerobotics.rov.event.io;

public interface Serializer {
    /**
     * Serialize the given object into a byte array.
     *
     * @param object the object to serialize
     */
    byte[] serialize(final Object object);

    /**
     * Deserialize the given bytes into an object.
     *
     * @param bytes the bytes to deserializer
     */
    Object deserialize(final byte[] bytes);
}
