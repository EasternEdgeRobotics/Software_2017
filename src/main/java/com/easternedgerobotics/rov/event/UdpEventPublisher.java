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
     * The default port to listen on.
     */
    private static final int DEFAULT_PORT = 10003;

    /**
     * The serializer.
     */
    private final Serializer serializer;

    /**
     * The {@link rx.subjects.Subject} used to pass values along to
     * subscribers.
     */
    private final PublishSubject<Object> subject;

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
     * Constructs an EventPublisher that broadcasts event to the given broadcast
     * address on the default port.
     *
     * @param broadcast the broadcast address to use when emitting events.
     */
    public UdpEventPublisher(final String broadcast) {
        this(new KryoSerializer(), DEFAULT_PORT, broadcast, DEFAULT_PORT);
    }

    /**
     * Constructs an EventPublisher with the given serializer and broadcast
     * address that listens on the given port.
     *
     * @param sr the serializer to use when emitting events
     * @param port the port to listen for connections on
     * @param broadcast the broadcast address to use when emitting events
     * @param broadcastPort the port for the broadcast address
     */
    public UdpEventPublisher(
        final Serializer sr,
        final int port,
        final String broadcast,
        final int broadcastPort
    ) {
        serializer = sr;
        subject = PublishSubject.create();
        outbound = RxNetty.createUdpClient(broadcast, broadcastPort).connect().toBlocking().first();
        values = new ConcurrentHashMap<>();
        server = RxNetty.createUdpServer(port, new UdpConnectionHandler(this::connection));
        server.start();
    }

    /**
     * Broadcasts the given value to the network.
     *
     * @param value the value to emit
     */
    public final <T extends MutableValueCompanion> void emit(final T value) {
        outbound.writeBytesAndFlush(serializer.serialize(value.asMutable()));
    }

    /**
     * Returns an {@link Observable} of the values of the given type.
     *
     * @param clazz the class type to filter values by
     * @return an Observable that emits each value of the given type
     */
    @SuppressWarnings("unchecked")
    public final <T extends MutableValueCompanion> Observable<T> valuesOfType(final Class<T> clazz) {
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

    @Override
    public final void await() throws InterruptedException {
        server.waitTillShutdown();
    }

    /**
     * Stops receiving and broadcasting events.
     */
    @Override
    public final void stop() {
        try {
            server.shutdown();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Invoked whenever a new connection is established.
     *
     * @param bytes the content of the connection
     */
    private void connection(final byte[] bytes) {
        subject.onNext(serializer.deserialize(bytes));
    }
}
