package com.easternedgerobotics.rov.event;

import com.easternedgerobotics.rov.event.io.Serializer;

import org.junit.Assert;
import org.junit.Test;

public class UdpEventPublisherTest {
    @Test
    public final void valuesOfTypeShouldReceiveEmittedValue() throws InterruptedException {
        final Serializer serializer = new TestValueSerializer();
        final UdpEventPublisher eventPublisherA = new UdpEventPublisher(serializer, 1234, "127.0.0.1", 4321);
        final UdpEventPublisher eventPublisherB = new UdpEventPublisher(serializer, 4321, "127.0.0.1", 1234);
        final TestObserver<TestValue> observer = new TestObserver<>(1);

        eventPublisherB.valuesOfType(TestValue.class).subscribe(observer);
        eventPublisherA.emit(new TestValue((byte) 42));

        observer.await();
        Assert.assertEquals(42, observer.getValue().getValue());

        eventPublisherA.stop();
        eventPublisherB.stop();
    }
}
