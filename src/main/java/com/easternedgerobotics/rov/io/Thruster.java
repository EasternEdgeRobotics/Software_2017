package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.ThrusterValue;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Thruster {

    private static final int BYTE_SIZE = 8;

    private static final byte WRITE_ADDRESS = 0x00;

    private static final byte READ_ADDRESS = 0x02;

    private static final int READ_BUFFER_SIZE = 9;

    private static final int VOLTAGE_INDEX = 2;

    private static final float VOLTAGE_SCALAR = 0.0004921f;

    private static final int CURRENT_INDEX = 6;

    private static final float CURRENT_SCALAR = 0.001122f;
    
    private static final int CURRENT_OFFSET = 32767;

    private static final int TEMPERATURE_INDEX = 4;
    
    // Constants for temperature calculation
    private static final int SERIESRESISTOR = 3300;
    
    private static final int THERMISTORNOMINAL = 10000;
    
    private static final int BCOEFFICIENT = 3900;
    
    private static final int TEMPERATURENOMINAL = 25;
    
    private static final float CELSIUSCONVERSION = 273.15f;
    //

    private final byte[] zeroBuffer = new byte[] {0x00, 0x00};

    private Device device;

    private ThrusterValue thrusterValue;

    private EventPublisher eventPublisher;
    
    private boolean initialized;

    public Thruster(final EventPublisher event, final ThrusterValue thruster, final Device dev) {

        device = dev;

        eventPublisher = event;

        thrusterValue = thruster;
        
        initialized = false;

        eventPublisher.valuesOfType(ThrusterValue.class).subscribe(t -> {
            if (thrusterValue.getName().equals(t.getName())) {
                thrusterValue = t;
            }
        });
    }

    public final void write() throws IOException {
        final short speed = (short) (thrusterValue.getSpeed() * Short.MAX_VALUE);
        final byte[] writeBuffer = ByteBuffer.allocate(2).putShort(speed).array();
        if (!initialized) {
            device.write(WRITE_ADDRESS, zeroBuffer);
            initialized = true;
        }
        device.write(WRITE_ADDRESS, writeBuffer);
    }

    public final void writeZero() throws IOException {
        device.write(WRITE_ADDRESS, zeroBuffer);
    }

    public final void read() throws IOException {
        final byte[] readBuffer = device.read(READ_ADDRESS, READ_BUFFER_SIZE);

        final float voltage = parseShort(readBuffer, VOLTAGE_INDEX) * VOLTAGE_SCALAR;
        final float current = (parseShort(readBuffer, CURRENT_INDEX) - CURRENT_OFFSET) * CURRENT_SCALAR;
        final float temperature = calculateTemperature(parseShort(readBuffer, TEMPERATURE_INDEX));

        eventPublisher.emit(ThrusterValue.create(
            thrusterValue.getName(),
            thrusterValue.getSpeed(),
            voltage,
            current,
            temperature
        ));
    }

    // reads a short from two consecutive bytes.
    // MSB located at offset.
    // LSB located at offset + 1.
    private short parseShort(final byte[] bytes, final int offset) {
        return (short) (((bytes[offset]) << BYTE_SIZE) | bytes[offset + 1]);
    }
    
    // Pulled from blueESC documentation
    // http://docs.bluerobotics.com/bluesc/
    private float calculateTemperature(final short tempRaw) {
        float steinhart;
        if (tempRaw == 0) {
            steinhart = 0;
        } else {
            final float resistance = SERIESRESISTOR / (65535 / tempRaw - 1);
            
            steinhart = resistance / THERMISTORNOMINAL;
            steinhart = (float) Math.log(steinhart);
            steinhart /= BCOEFFICIENT;
            steinhart += 1.0f / (TEMPERATURENOMINAL + CELSIUSCONVERSION);
            steinhart = 1.0f / steinhart;
            steinhart -= CELSIUSCONVERSION;
        }
        return steinhart;
    }
}
