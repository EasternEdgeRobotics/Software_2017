package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.MockLaunchConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.Bar30PressureSensor;
import com.easternedgerobotics.rov.io.rpi.RaspberryI2CBus;

import com.pi4j.io.i2c.I2CBus;
import rx.Observable;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public final class Bar30PressureSensorTest {
    private Bar30PressureSensorTest() {

    }

    public static void main(final String[] args) throws UnknownHostException, SocketException, InterruptedException {
        final LaunchConfig launchConfig = new MockLaunchConfig();
        final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
        final int broadcastPort = launchConfig.defaultBroadcastPort();
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            new DatagramSocket(broadcastPort), broadcastAddress, broadcastPort, new BasicOrder<>()));

        final int duration = 500;
        final Bar30PressureSensor bar30PressureSensor = new Bar30PressureSensor(
            new RaspberryI2CBus(I2CBus.BUS_1).get(Bar30PressureSensor.ADDRESS), Schedulers.newThread(), duration);

        final int sleepTime = 1000;
        Thread.sleep(sleepTime);

        Observable.interval(duration, TimeUnit.MILLISECONDS, Schedulers.io()).subscribe(tick -> {
            eventPublisher.emit(bar30PressureSensor.pressure());
            eventPublisher.emit(bar30PressureSensor.temperature());
        });

        eventPublisher.await();
    }
}
