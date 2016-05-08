package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.pololu.PololuMaestro;
import com.easternedgerobotics.rov.io.pololu.PololuMaestroOutputChannel;
import com.easternedgerobotics.rov.value.SpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import rx.Observable;
import rx.broadcast.SingleSourceFifoOrder;
import rx.broadcast.UdpBroadcast;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class IndividualMotorTest {
    private IndividualMotorTest() {

    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final byte deviceNumber = Byte.parseByte(args[0]);
        final byte channel = Byte.parseByte(args[1]);
        final int baudRate = 115200;

        final Serial serial = SerialFactory.createInstance();
        final PololuMaestro maestro = new PololuMaestro(serial, deviceNumber);
        serial.open("/dev/ttyACM0", baudRate);

        final InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            new DatagramSocket(broadcastPort), broadcastAddress, broadcastPort, new SingleSourceFifoOrder<>()));

        final Observable<SpeedValue> speeds = eventPublisher.valuesOfType(SpeedValue.class);
        final SpeedValue speed = new SpeedValue("Test");

        final Motor motor = new Motor(
            speeds.filter(x -> x.getName().equals("Test")),
            new PololuMaestroOutputChannel(maestro, channel, Motor.MAX_FWD, Motor.MAX_REV));

        System.out.println("Test Started");
        System.out.println("Motor is stopped");
        Thread.sleep(1000);

        eventPublisher.emit(speed.setSpeed(.5f));
        Thread.sleep(1000);
        System.out.println("Motor on half power clockwise");
        motor.write();

        eventPublisher.emit(speed.setSpeed(0f));
        Thread.sleep(1000);
        System.out.println("Motor is stopped");
        motor.write();

        eventPublisher.emit(speed.setSpeed(-.5f));
        Thread.sleep(1000);
        System.out.println("Motor on half power counter-clockwise");
        motor.write();

        Thread.sleep(1000);
        System.out.println("Motor is stopped");
        motor.writeZero();
    }
}
