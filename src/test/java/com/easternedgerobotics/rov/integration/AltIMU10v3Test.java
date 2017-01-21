package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.pololu.AltIMU10v3;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.RotationValue;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import rx.Observable;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public final class AltIMU10v3Test {
    private AltIMU10v3Test() {

    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        final InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                new DatagramSocket(broadcastPort), broadcastAddress, broadcastPort, new BasicOrder<>()));

        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        // We want to ground SA0 because its easier than putting power to it
        // Therefore the last address bit will be low
        final AltIMU10v3 imu = new AltIMU10v3(bus, false, Observable.interval(100, TimeUnit.MILLISECONDS));

        imu.pressure().subscribe(eventPublisher::emit);
        imu.rotation().subscribe(eventPublisher::emit);
        imu.acceleration().subscribe(eventPublisher::emit);
        imu.angularVelocity().subscribe(eventPublisher::emit);
        imu.temperature().subscribe(eventPublisher::emit);

        Observable.zip(
            eventPublisher.valuesOfType(InternalPressureValue.class),
            eventPublisher.valuesOfType(RotationValue.class),
            eventPublisher.valuesOfType(AccelerationValue.class),
            eventPublisher.valuesOfType(AngularVelocityValue.class),
            eventPublisher.valuesOfType(InternalTemperatureValue.class),
            (p, r, a, v, t) ->
                String.format("P: %s\t\t\tR: %s\t\t\tA: %s\t\t\tV %s\t\t\tT: %s",
                    String.valueOf(p.getPressure()),
                    String.format("%d\t%d\t%d", (int) r.getX(), (int) r.getY(), (int) r.getZ()),
                    String.format("%d\t%d\t%d", (int) a.getX(), (int) a.getY(), (int) a.getZ()),
                    String.format("%d\t%d\t%d", (int) v.getX(), (int) v.getY(), (int) v.getZ()),
                    String.valueOf(t.getTemperature())))
            .subscribe(System.out::println);

        eventPublisher.await();
    }
}
