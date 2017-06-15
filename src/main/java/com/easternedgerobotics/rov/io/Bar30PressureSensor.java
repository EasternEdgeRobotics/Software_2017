package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.devices.Barometer;
import com.easternedgerobotics.rov.io.devices.I2C;
import com.easternedgerobotics.rov.io.devices.Thermometer;
import com.easternedgerobotics.rov.value.ExternalPressureValue;
import com.easternedgerobotics.rov.value.ExternalTemperatureValue;
import com.easternedgerobotics.rov.value.PressureValue;
import com.easternedgerobotics.rov.value.TemperatureValue;

import org.pmw.tinylog.Logger;
import rx.Scheduler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * For information about the algorithms and constants chosen.
 * https://github.com/bluerobotics/BlueRobotics_MS5837_Library/blob/master/MS5837.cpp
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class Bar30PressureSensor implements Barometer, Thermometer {
    public static final byte ADDRESS = 0x76;

    private static final byte RESET = 0x1E;

    private static final byte ADC_READ = 0x00;

    private static final byte PROM_READ = (byte) 0xA0;

    private static final byte CONVERT_D1 = 0x4A;

    private static final byte CONVERT_D2 = 0x5A;

    private final AtomicReference<PressureValue> pressure = new AtomicReference<>(new ExternalPressureValue());

    private final AtomicReference<TemperatureValue> temperature = new AtomicReference<>(new ExternalTemperatureValue());

    private final AtomicBoolean firstReading = new AtomicBoolean(true);

    private final I2C channel;

    private final Scheduler.Worker worker;

    private final long conversionTime;

    public Bar30PressureSensor(final I2C channel, final Scheduler scheduler, final long conversionTime) {
        this.channel = channel;
        this.worker = scheduler.createWorker();
        this.conversionTime = conversionTime;
        init();
    }

    @Override
    public PressureValue pressure() {
        return pressure.get();
    }

    @Override
    public TemperatureValue temperature() {
        return temperature.get();
    }

    private void init() {
        worker.schedule(() -> {
            channel.write(RESET);
            worker.schedule(() -> {
                final long[] crc = new long[8];
                for (byte i = 0; i < 7; i++) {
                    final byte[] c = channel.read((byte) (PROM_READ + i * 2), 2);
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    byteBuffer.put((byte) 0);
                    byteBuffer.put((byte) 0);
                    byteBuffer.put(c[0]);
                    byteBuffer.put(c[1]);
                    byteBuffer.flip();
                    crc[i] = byteBuffer.asIntBuffer().get();
                }
                final long crcRead = crc[0] >> 12;
                final long crcCalculated = crc4(crc);

                if (crcCalculated == crcRead) {
                    worker.schedule(() -> this.convert(crc));
                } else {
                    Logger.error("Could not initialize the MS5837 pressure sensor");
                }
            }, conversionTime, TimeUnit.MILLISECONDS);
        });
    }

    private static long crc4(final long[] prom) {
        long remainder = 0;
        prom[0] = (prom[0]) & 0x0FFF;
        prom[7] = 0;

        for (int i = 0; i < 16; i++) {
            if (i % 2 == 1) {
                remainder ^= prom[i >> 1] & 0x00FF;
            } else {
                remainder ^= prom[i >> 1] >> 8;
            }
            for (int nBit = 8; nBit > 0; nBit--) {
                if ((remainder & 0x8000) != 0) {
                    remainder = (remainder << 1) ^ 0x3000;
                } else {
                    remainder = remainder << 1;
                }
            }
        }
        remainder = (remainder >> 12) & 0x000F;
        return remainder;
    }

    private void convert(final long[] crc) {
        channel.write(CONVERT_D1);
        worker.schedule(() -> {
            final byte[] d1Bytes;
            try {
                d1Bytes = channel.readUnsafe(ADC_READ, 3);
            } catch (final IOException e) {
                worker.schedule(() -> convert(crc));
                return;
            }
            final ByteBuffer d1ByteBuffer = ByteBuffer.allocate(4);
            d1ByteBuffer.put((byte) 0);
            d1ByteBuffer.put(d1Bytes[0]);
            d1ByteBuffer.put(d1Bytes[1]);
            d1ByteBuffer.put(d1Bytes[2]);
            d1ByteBuffer.flip();
            final int d1 = d1ByteBuffer.asIntBuffer().get();
            channel.write(CONVERT_D2);
            worker.schedule(() -> {
                final byte[] d2Bytes;
                try {
                    d2Bytes = channel.readUnsafe(ADC_READ, 3);
                } catch (final IOException e) {
                    worker.schedule(() -> convert(crc));
                    return;
                }
                final ByteBuffer d2ByteBuffer = ByteBuffer.allocate(4);
                d2ByteBuffer.put((byte) 0);
                d2ByteBuffer.put(d2Bytes[0]);
                d2ByteBuffer.put(d2Bytes[1]);
                d2ByteBuffer.put(d2Bytes[2]);
                d2ByteBuffer.flip();
                final int d2 = d2ByteBuffer.asIntBuffer().get();
                calculate(crc, d1, d2);
                worker.schedule(() -> convert(crc));
            }, conversionTime, TimeUnit.MILLISECONDS);
        }, conversionTime, TimeUnit.MILLISECONDS);
    }

    private void calculate(final long[] crc, final int d1, final int d2) {

        final long dT = d2 - crc[5] * 256;
        final long sens1 = crc[1] * 32768 + (crc[3] * dT) / 256;
        final long off1 = crc[2] * 65536 + (crc[4] * dT) / 128;
        final long t1 = 2000 + dT * crc[6] / 8388608;

        long sens2 = 0;
        long off2 = 0;
        long t2 = 0;

        if (t1 >= 2000) {
            t2 = 2 * (dT * dT) / 137438953472L;
            off2 = ((t1 - 2000) * (t1 - 2000)) / 16;
            sens2 = 0;
        } else if (t1 < 2000) {
            t2 = 3 * (dT * dT) / 8589934592L;
            off2 = 3 * ((t1 - 2000) * (t1 - 2000)) / 2;
            sens2 = 5 * ((t1 - 2000) * (t1 - 2000)) / 8;
            if (t1 < -1500) {
                off2 = off2 + 7 * ((t1 + 1500) * (t1 + 1500));
                sens2 = sens2 + 4 * ((t1 + 1500) * (t1 + 1500));
            }
        }

        final double t = t1 - t2;
        final double off = off1 - off2;
        final double sens = sens1 - sens2;

        final float tFinal = (float) (t / 100.0);
        final float pFinal = (float) ((((d1 * sens) / 2097152) - off) / 8192) / 100;
        if (firstReading.getAndSet(false)
            || (Math.abs(tFinal - temperature.get().getTemperature()) / temperature.get().getTemperature() < 0.1
            && Math.abs(pFinal - pressure.get().getPressure()) / pressure.get().getPressure() < 0.1)
        ) {
            temperature.set(new ExternalTemperatureValue(tFinal));
            pressure.set(new ExternalPressureValue(pFinal));
        }
    }
}
