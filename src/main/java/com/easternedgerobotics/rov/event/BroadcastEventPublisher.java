package com.easternedgerobotics.rov.event;

import rx.Observable;
import rx.broadcast.Broadcast;

import java.util.concurrent.CountDownLatch;

public final class BroadcastEventPublisher implements EventPublisher {
    /**
     * The default port to broadcast messages on.
     */
    public static final int DEFAULT_BROADCAST_PORT = 10003;

    private final Broadcast broadcast;

    private final CountDownLatch countDownLatch;

    /**
     * Constructs a new {@code BroadcastEventPublisher} from the given {@link Broadcast} instance.
     * @param broadcast the broadcast to use for events
     */
    public BroadcastEventPublisher(final Broadcast broadcast) {
        this.broadcast = broadcast;
        this.countDownLatch = new CountDownLatch(1);
    }

    /**
     * Broadcasts the given value to the network.
     *
     * @param value the value to emit
     */
    @Override
    public final void emit(final Object value) {
        broadcast.send(value).toBlocking().subscribe();
    }

    /**
     * Returns an {@link Observable} of the values of the given type.
     *
     * @param clazz the class type to filter values by
     * @return an Observable that emits each value of the given type
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return broadcast.valuesOfType(clazz);
    }

    /**
     * Stops receiving and broadcasting events.
     */
    @Override
    public final void stop() {
        countDownLatch.countDown();
    }

    /**
     * Block until this event publisher completes.
     */
    @Override
    public final void await() throws InterruptedException {
        countDownLatch.await();
    }
}
