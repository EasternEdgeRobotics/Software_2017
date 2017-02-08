package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.CpuValue;

import com.pi4j.system.SystemInfo;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("InnerAssignment")
public final class CpuInformation {
    private final Observable<Long> interval;

    private static final boolean RASPBIAN;

    static {
        boolean raspbian = false;
        final File file = new File("/etc", "os-release");
        if (file.exists()) {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String line;
                while (!raspbian && (line = br.readLine()) != null) {
                    line = line.toLowerCase();
                    raspbian = line.contains("raspbian") && line.contains("name");
                }
            } catch (final Exception e) {
                raspbian = false;
            }
        }
        RASPBIAN = raspbian;
    }

    /**
     * Constructs a CpuInformation instance that polls CPU properties at the specified interval.
     *
     * @param interval the interval at which to poll the CPU properties.
     * @param timeUnit the {@code TimeUnit} the interval is specified in.
     */
    public CpuInformation(final long interval, final TimeUnit timeUnit) {
        this.interval = Observable.interval(interval, timeUnit);
    }

    /**
     * Returns an observable stream of CPU values.
     *
     * @return a stream of CPU values.
     */
    public final Observable<CpuValue> observe() {
        return interval.observeOn(Schedulers.io()).map(this::pollCpu);
    }

    private CpuValue pollCpu(final long tick) {
        if (RASPBIAN) {
            try {
                return new CpuValue(
                    SystemInfo.getClockFrequencyArm(),
                    SystemInfo.getCpuTemperature(),
                    SystemInfo.getCpuVoltage());
            } catch (final InterruptedException | IOException e) {
                throw Exceptions.propagate(e);
            }
        }
        return new CpuValue();
    }
}
