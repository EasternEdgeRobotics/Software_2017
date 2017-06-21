package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.MockLaunchConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.DigitalLight;
import com.easternedgerobotics.rov.io.rpi.RPiGPIOLight;
import com.easternedgerobotics.rov.value.LightValue;
import com.easternedgerobotics.rov.value.TestLightValue;

import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class IndividualLightTest {
    private IndividualLightTest() {

    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final LaunchConfig launchConfig = new MockLaunchConfig();
        final byte pinNumber = Byte.parseByte(args[0]);

        final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
        final int broadcastPort = launchConfig.defaultBroadcastPort();
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            new DatagramSocket(broadcastPort), broadcastAddress, broadcastPort, new BasicOrder<>()));

        final DigitalLight light = new DigitalLight(
            eventPublisher.valuesOfType(TestLightValue.class).cast(LightValue.class),
            new RPiGPIOLight(pinNumber));

        System.out.println("Test Started");
        System.out.println("Light is off");
        Thread.sleep(1000);

        eventPublisher.emit(new TestLightValue(true));
        Thread.sleep(1000);
        System.out.println("Light is at full brightness");
        light.write();

        eventPublisher.emit(new TestLightValue(false));
        Thread.sleep(1000);
        System.out.println("Light is off");
        light.write();

        eventPublisher.emit(new TestLightValue(true));
        Thread.sleep(1000);
        System.out.println("Light is at full brightness");
        light.write();

        Thread.sleep(1000);
        System.out.println("Light is off");
        light.writeZero();
    }
}
