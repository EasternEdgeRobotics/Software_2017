package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.TestSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class IndividualThrusterTest {
    private IndividualThrusterTest() {

    }

    private static final float SAFE_FOR_AIR_THRUSTER_POWER_RATIO = 0.2f;

    private static final int VALUE_WAIT = 100;

    private static final int DURATION = 1000;

    private static final List<Byte> ADDRESSES = Arrays.asList(
            (byte) 0,
            (byte) 1,
            (byte) 2,
            (byte) 15,
            (byte) 16,
            (byte) 17);

    public static void main(final String[] args) throws InterruptedException, IOException {
        final byte maestroDeviceNumber = 0x01;
        final int baud = 115200;
        final Serial serial = SerialFactory.createInstance();
        serial.open("/dev/ttyACM0", baud);

        final Maestro<?> maestro = new Maestro<>(serial, maestroDeviceNumber);

        for (final byte address : ADDRESSES) {
            final PublishSubject<SpeedValue> speed = PublishSubject.create();
            final Thruster thruster = new Thruster(
                speed, maestro.get(address).setOutputRange(new Range(Thruster.MAX_REV, Thruster.MAX_FWD)));

            speed.onNext(new TestSpeedValue(1 * SAFE_FOR_AIR_THRUSTER_POWER_RATIO));
            System.out.println("Spinning thruster " + address);
            Thread.sleep(VALUE_WAIT);
            thruster.write();
            Thread.sleep(DURATION);
            System.out.println("Stopped thruster " + address);
            thruster.writeZero();
        }
    }
}
