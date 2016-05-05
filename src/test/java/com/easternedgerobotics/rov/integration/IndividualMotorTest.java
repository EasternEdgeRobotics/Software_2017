package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.pololu.PololuMaestro;
import com.easternedgerobotics.rov.io.pololu.PololuMaestroOutputChannel;
import com.easternedgerobotics.rov.value.SpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

import rx.Observable;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class IndividualMotorTest {
    private IndividualMotorTest() {

    }

    public static void main(final String[] args) throws InterruptedException {
        final byte deviceNumber = Byte.parseByte(args[0]);
        final byte channel = Byte.parseByte(args[1]);
        final int baudRate = 115200;

        final Serial serial = SerialFactory.createInstance();
        final PololuMaestro maestro = new PololuMaestro(serial, deviceNumber);
        serial.open("/dev/ttyACM0", baudRate);

        final EventPublisher eventPublisher = new UdpEventPublisher("255.255.255.255");

        final Observable<SpeedValue> speeds = eventPublisher.valuesOfType(SpeedValue.class);
        final SpeedValue speed = SpeedValue.zero("Test");

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
