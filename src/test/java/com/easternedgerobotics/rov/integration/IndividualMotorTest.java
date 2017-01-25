package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.TestSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

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
        serial.open("/dev/ttyACM0", baudRate);

        final Motor motor = new Motor(
            new Maestro<>(serial, deviceNumber).get(channel).setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD)));

        System.out.println("Test Started");
        System.out.println("Motor is stopped");
        Thread.sleep(1000);

        Thread.sleep(1000);
        System.out.println("Motor on half power clockwise");
        motor.write(new TestSpeedValue(.5f));

        Thread.sleep(1000);
        System.out.println("Motor is stopped");
        motor.write(new TestSpeedValue(0f));

        Thread.sleep(1000);
        System.out.println("Motor on half power counter-clockwise");
        motor.write(new TestSpeedValue(-.5f));

        Thread.sleep(1000);
        System.out.println("Motor is stopped");
        motor.writeZero();
    }
}
