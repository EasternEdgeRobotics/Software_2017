package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.io.pololu.PololuMaestro;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

import java.util.BitSet;

public final class PololuMaestroGetPositionTest {
    private PololuMaestroGetPositionTest() {

    }

    public static void main(final String[] args) {
        final byte deviceNumber = Byte.parseByte(args[0]);
        final byte channel = Byte.parseByte(args[1]);
        final int baudRate = 115_200;
        final Serial serial = SerialFactory.createInstance();
        final PololuMaestro maestro = new PololuMaestro(serial, deviceNumber);

        serial.open("/dev/ttyACM0", baudRate);
        System.out.printf("Position of channel %d is %d%n", channel, maestro.getPosition(channel));
        final BitSet errors = maestro.getErrors();
        if (!errors.isEmpty()) {
            System.out.printf("Errors set is %s%n", errors);
        }
    }
}
