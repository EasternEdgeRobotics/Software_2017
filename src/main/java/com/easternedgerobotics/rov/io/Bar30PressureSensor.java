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

    private final I2C channel;

    private final Scheduler scheduler;

    private final long conversionTime;

    private final AtomicBoolean ready = new AtomicBoolean(false);

    public Bar30PressureSensor(final I2C channel, final Scheduler scheduler, final long conversionTime) {
        this.channel = channel;
        this.scheduler = scheduler;
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
        scheduler.createWorker().schedule(() -> {
            channel.write(RESET);
            scheduler.createWorker().schedule(() -> {
                final short[] crc = new short[8];
                for (byte i = 0; i < 7; i++) {
                    final byte[] c = channel.read((byte) (PROM_READ + i * 2), 2);
                    crc[i] = (short) ((c[0] << 8) | c[1]);
                }
                final byte crcRead = (byte) (crc[0] >> 12);
                final byte crcCalculated = crc4(crc);

                if (crcCalculated == crcRead) {
                    ready.set(true);
                    scheduler.createWorker().schedule(() -> this.convert(crc));
                } else {
                    Logger.error("Could not initialize the MS5837 pressure sensor");
                }
            }, conversionTime, TimeUnit.MILLISECONDS);
        });
    }

    private static byte crc4(final short[] prom) {
        short remainder = 0;

        prom[0] = (short) ((prom[0]) & 0x0FFF);
        prom[7] = 0;

        for (byte i = 0; i < 16; i++) {
            if (i % 2 == 1) {
                remainder ^= (short) ((prom[i >> 1]) & 0x00FF);
            } else {
                remainder ^= (short) (prom[i >> 1] >> 8);
            }
            for (byte nBit = 8; nBit > 0; nBit--) {
                if ((remainder & 0x8000) != 0) {
                    remainder = (short) ((remainder << 1) ^ 0x3000);
                } else {
                    remainder = (short) (remainder << 1);
                }
            }
        }

        remainder = (short) ((remainder >> 12) & 0x000F);

        return (byte) remainder;
    }

    private void convert(final short[] crc) {
        channel.write(CONVERT_D1);
        scheduler.createWorker().schedule(() -> {
            final byte[] d1Bytes = channel.read(ADC_READ, 3);
            final int d1 = (((d1Bytes[0] << 8) | d1Bytes[1]) << 8) | d1Bytes[2];
            channel.write(CONVERT_D2);
            scheduler.createWorker().schedule(() -> {
                channel.write(ADC_READ);
                final byte[] d2Bytes = channel.read(ADC_READ, 3);
                final int d2 = (((d2Bytes[0] << 8) | d2Bytes[1]) << 8) | d2Bytes[2];
                calculate(crc, d1, d2);
                scheduler.createWorker().schedule(() -> convert(crc));
            }, conversionTime, TimeUnit.MILLISECONDS);

        }, conversionTime, TimeUnit.MILLISECONDS);
    }

    private void calculate(final short[] crc, final int d1, final int d2) {

        final double dT = d2 - crc[5] * 256.0;
        final double sens = crc[1] * 32768.0 + (crc[3] * dT) / 256.0;
        final double off = crc[2] * 65536.0 + (crc[4] * dT) / 128.0;
        final double t = 2000.0 + dT * crc[6] / 8388608.0;

        double iSens = 0;
        double iOff = 0;
        double iT = 0;

        if (t / 100.0 < 20.0) {
            // low temp
            iT = 3.0 * dT * dT / 8589934592.0;
            iOff = 3.0 * (t - 2000.0) * (t - 2000.0) / 2.0;
            iSens = 5.0 * (t - 2000.0) * (t - 2000.0) / 8.0;
            if (t / 100.0 < -15.0) {
                //Very low temp
                iOff = iOff + 7 * (t + 1500.0) * (t + 1500.0);
                iSens = iSens + 4 * (t + 1500.0) * (t + 1500.0);
            }
        } else if (t / 100.0 >= 20.0) {
            //High temp
            iT = 2.0 * dT * dT / 137438953472.0;
            iOff = 1.0 * (t - 2000.0) * (t - 2000.0) / 16.0;
            iSens = 0.0;
        }

        final double off2 = off - iOff;
        final double sens2 = sens - iSens;

        temperature.set(new ExternalTemperatureValue((float) ((t - iT)  / 100.0)));
        pressure.set(new ExternalPressureValue((float) ((((d1 * sens2) / 2097152.0 - off2) / 8192.0) / 10)));
    }
}
