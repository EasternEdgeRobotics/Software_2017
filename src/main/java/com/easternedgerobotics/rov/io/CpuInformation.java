package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.CpuValue;

import com.pi4j.system.SystemInfo;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class CpuInformation {
    public CpuValue pollCpu() {
        if (Files.notExists(Paths.get("/opt/vc/bin/vcgencmd"))) {
            return new CpuValue();
        }
        try {
            return new CpuValue(
                SystemInfo.getClockFrequencyArm(), SystemInfo.getCpuTemperature(), SystemInfo.getCpuVoltage());
        } catch (final InterruptedException | IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
