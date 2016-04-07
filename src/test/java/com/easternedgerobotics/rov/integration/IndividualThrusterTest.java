package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.test.CarelessConsumer;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public class IndividualThrusterTest {
    private static final float SAFE_FOR_AIR_THRUSTER_POWER_RATIO = 0.1f;

    private static final int DURATION = 5;

    @Parameterized.Parameters(name = "Address {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {(byte) 0x2A, DURATION},
            {(byte) 0x2B, DURATION},
            {(byte) 0x2C, DURATION},
            {(byte) 0x2D, DURATION},
            {(byte) 0x2E, DURATION},
            {(byte) 0x2F, DURATION},
            {(byte) 0x30, DURATION},
            {(byte) 0x31, DURATION},
        });
    }

    private final byte address;

    private final int duration;

    private EventPublisher eventPublisher;

    public IndividualThrusterTest(final Byte address, final Integer duration) {
        this.address = address;
        this.duration = duration;
    }

    @Before
    public final void before() {
        eventPublisher = new UdpEventPublisher("192.168.88.255");
    }

    @After
    public final void after() {
        eventPublisher.stop();
    }

    @Test
    public final void individualThruster() throws InterruptedException, IOException {
        Assume.assumeThat(
            "This test requires a Raspberry Pi", System.getProperty("os.arch"), CoreMatchers.is("arm"));

        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        final ThrusterSpeedValue portFore = ThrusterSpeedValue.create("???");
        final Thruster thruster = new Thruster(eventPublisher, portFore, bus.getDevice(address));
        final CarelessConsumer<ThrusterSpeedValue> consumer = val -> thruster.write();

        eventPublisher.valuesOfType(ThrusterSpeedValue.class).subscribe(consumer::accept);
        eventPublisher.emit(ThrusterSpeedValue.create("???", 1 * SAFE_FOR_AIR_THRUSTER_POWER_RATIO));

        TimeUnit.SECONDS.sleep(duration);
        thruster.writeZero();
    }
}
