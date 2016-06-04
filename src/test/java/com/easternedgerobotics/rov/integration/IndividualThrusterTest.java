package com.easternedgerobotics.rov.integration;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.io.pololu.Maestro;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.TestSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RunWith(Parameterized.class)
@SuppressWarnings({"checkstyle:magicnumber"})
public class IndividualThrusterTest {
    private static final float SAFE_FOR_AIR_THRUSTER_POWER_RATIO = 0.1f;

    private static final int DURATION = 5;

    @Parameterized.Parameters(name = "Address {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {(byte) 12, DURATION},
            {(byte) 13, DURATION},
            {(byte) 14, DURATION},
            {(byte) 15, DURATION},
            {(byte) 16, DURATION},
            {(byte) 17, DURATION},
        });
    }

    private final byte address;

    private final int duration;

    private DatagramSocket socket;

    private EventPublisher eventPublisher;

    public IndividualThrusterTest(final Byte address, final Integer duration) {
        this.address = address;
        this.duration = duration;
    }

    @Before
    public final void before() throws SocketException, UnknownHostException {
        final InetAddress broadcastAddress = InetAddress.getByName("192.168.88.255");
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        socket = new DatagramSocket(broadcastPort);
        eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
    }

    @After
    public final void after() {
        eventPublisher.stop();
        socket.close();
    }

    @Test
    public final void individualThruster() throws InterruptedException, IOException {
        Assume.assumeThat(
            "This test requires a Raspberry Pi", System.getProperty("os.arch"), CoreMatchers.is("arm"));

        final byte maestroDeviceNumber = 0x01;
        final Serial serial = SerialFactory.createInstance();
        final Thruster thruster = new Thruster(
            eventPublisher.valuesOfType(TestSpeedValue.class).cast(SpeedValue.class),
            new Maestro<>(serial, maestroDeviceNumber)
                .get(address)
                .setOutputRange(new Range(Thruster.MAX_REV, Thruster.MAX_FWD)));
        final Consumer<SpeedValue> consumer = val -> thruster.write();

        serial.open("/dev/ttyACM0", 115_200);
        eventPublisher.valuesOfType(TestSpeedValue.class).cast(SpeedValue.class).subscribe(consumer::accept);
        eventPublisher.emit(new TestSpeedValue(1 * SAFE_FOR_AIR_THRUSTER_POWER_RATIO));

        TimeUnit.SECONDS.sleep(duration);
        thruster.writeZero();
    }
}
