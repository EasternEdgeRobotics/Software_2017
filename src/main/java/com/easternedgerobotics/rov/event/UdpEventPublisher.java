package com.easternedgerobotics.rov.event;

import com.easternedgerobotics.rov.event.io.KryoSerializer;
import com.easternedgerobotics.rov.event.io.Serializer;
import com.easternedgerobotics.rov.value.ImmutableValueCompanion;
import com.easternedgerobotics.rov.value.MutableValueCompanion;

import io.netty.channel.socket.DatagramPacket;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.protocol.udp.server.UdpServer;
import rx.Observable;
import rx.subjects.Subject;
import rx.subjects.PublishSubject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The EventPublisher class that serves as both a way to subscribe to events
 * and to emit events.
 * <p>
 * This class uses UDP to transport events across a network, broadcasting
 * each emitted event to all nodes addressable by the address used to construct
 * the instance.
 */
public class UdpEventPublisher implements EventPublisher {
    /**
     * The serializer.
     */
    private final Serializer serializer;

    /**
     * The @{link rx.subjects.Subject} used to pass values along to
     * subscribers.
     */
    private final Subject subject;

    /**
     * The outbound broadcast connection.
     */
    private final ObservableConnection<DatagramPacket, DatagramPacket> outbound;

    /**
     * The cache for the value streams.
     */
    private final ConcurrentHashMap<Class, Object> values;

    /**
     * The UDP server.
     */
    private final UdpServer<DatagramPacket, DatagramPacket> server;

    /**
     * Constructs an EventPublisher that uses the given broadcast address
     * and listens on port 10003.
     *
     * @param broadcast the broadcast address to use when emitting events
     */
    public UdpEventPublisher(final String broadcast) {
        this(broadcast, 10003);
    }

    /**
     * Constructs an EventPublisher that uses the given broadcast
     * address and listens on the given port.
     *
     * @param broadcast the broadcast address to use when emitting events
     * @param port the port to listen for connections on
     */
    public UdpEventPublisher(final String broadcast, final int port) {
        this(new KryoSerializer(), broadcast, port);
    }

    /**
     * Constructs an EventPublisher with the given serializer and broadcast
     * address that listens on the given port.
     *
     * @param sr the serializer to use when emitting events
     * @param broadcast the broadcast address to use when emitting events
     * @param port the port to listen for connections on
     */
    public UdpEventPublisher(final Serializer sr, final String broadcast, final int port) {
        serializer = sr;
        subject = PublishSubject.create();
        outbound = RxNetty.createUdpClient(broadcast, port).connect().toBlocking().first();
        values = new ConcurrentHashMap<Class, Object>();
        server = RxNetty.createUdpServer(port, new UdpConnectionHandler(this::connection));
    }

    /**
     * Broadcasts the given value to the network.
     *
     * @param value the value to emit
     */
    public <T extends MutableValueCompanion> void emit(final T value) {
        outbound.writeBytesAndFlush(serializer.serialize(value.asMutable()));
    }

    /**
     * Returns an @{link rx.Observable} of the values of the given type.
     *
     * @param clazz the class type to filter values by
     * @return an Observable that emits each value of the given type
     */
    @SuppressWarnings("unchecked")
    public <T extends MutableValueCompanion> Observable<T> valuesOfType(final Class<T> clazz) {
        if (values.containsKey(clazz)) {
            return (Observable<T>) values.get(clazz);
        }

        final Observable<T> observable =
            subject
                .map(v -> ((ImmutableValueCompanion) v).asImmutable())
                .filter(clazz::isInstance)
                .cast(clazz)
                .share();

        values.put(clazz, observable);
        return observable;
    }

    /**
     * Invoked whenever a new connection is established.
     *
     * @param bytes the content of the connection
     */
    private void connection(byte[] bytes) {
        subject.onNext(serializer.deserialize(bytes));
    }
}
