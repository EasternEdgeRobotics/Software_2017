package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.io.Motor;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.TestSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

@SuppressWarnings({"checkstyle:magicnumber"})
public final class IndividualMotorTest {
    private IndividualMotorTest() {

    }

    public static void main(final String[] args) throws
        IOException,
        InterruptedException,
        SocketException,
        UnknownHostException {
        final byte maestroAddress = 0x01;
        final int baud = 115200;
        final Serial serial = SerialFactory.createInstance();
        serial.open("/dev/ttyACM0", baud);
        final int channel = Integer.valueOf(args[0]);

        final PublishSubject<SpeedValue> speed = PublishSubject.create();
        final Motor motor = new Motor(
            speed,
            new Maestro<>(serial, maestroAddress).get(channel).setOutputRange(new Range(Motor.MAX_REV, Motor.MAX_FWD)));

        speed.onNext(new TestSpeedValue(1f));
        Thread.sleep(1000);
        motor.write();
        Thread.sleep(200);
        motor.writeZero();
    }
}
