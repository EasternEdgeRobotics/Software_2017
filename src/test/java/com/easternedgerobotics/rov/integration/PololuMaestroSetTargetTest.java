package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.io.pololu.PololuMaestro;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

import java.util.BitSet;

public final class PololuMaestroSetTargetTest {
    private PololuMaestroSetTargetTest() {

    }

    public static void main(final String[] args) {
        final byte deviceNumber = Byte.parseByte(args[0]);
        final byte channel = Byte.parseByte(args[1]);
        final short microseconds = Short.parseShort(args[2]);
        final int baudRate = 115_200;
        final Serial serial = SerialFactory.createInstance();
        final PololuMaestro maestro = new PololuMaestro(serial, deviceNumber);

        System.out.printf("Set target to %dµs%n", microseconds);

        serial.open("/dev/ttyACM0", baudRate);
        maestro.setTarget(channel, microseconds);
        final BitSet errors = maestro.getErrors();
        if (!errors.isEmpty()) {
            System.out.printf("Errors set is %s%n", errors);
        }
    }
}
