package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.Light;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.TestSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
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
        final byte deviceNumber = Byte.parseByte(args[0]);
        final byte channel = Byte.parseByte(args[1]);
        final int baudRate = 115200;

        final Serial serial = SerialFactory.createInstance();
        serial.open("/dev/ttyACM0", baudRate);

        final InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            new DatagramSocket(broadcastPort), broadcastAddress, broadcastPort, new BasicOrder<>()));

        final Light light = new Light(
            eventPublisher.valuesOfType(TestSpeedValue.class).cast(SpeedValue.class),
            new Maestro<>(serial, deviceNumber).get(channel).setOutputRange(new Range(Light.MAX_REV, Light.MAX_FWD)));

        System.out.println("Test Started");
        System.out.println("Light is off");
        Thread.sleep(1000);

        eventPublisher.emit(new TestSpeedValue(.5f));
        Thread.sleep(1000);
        System.out.println("Light is at half brightness");
        light.write();

        eventPublisher.emit(new TestSpeedValue(0f));
        Thread.sleep(1000);
        System.out.println("Light is off");
        light.write();

        eventPublisher.emit(new TestSpeedValue(1f));
        Thread.sleep(1000);
        System.out.println("Light is at full brightness");
        light.write();

        Thread.sleep(1000);
        System.out.println("Light is off");
        light.writeZero();
    }
}
