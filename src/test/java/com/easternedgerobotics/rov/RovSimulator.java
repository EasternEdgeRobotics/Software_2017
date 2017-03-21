package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.MockRovConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.ADC;
import com.easternedgerobotics.rov.io.Accelerometer;
import com.easternedgerobotics.rov.io.Barometer;
import com.easternedgerobotics.rov.io.Gyroscope;
import com.easternedgerobotics.rov.io.Magnetometer;
import com.easternedgerobotics.rov.io.PWM;
import com.easternedgerobotics.rov.io.Thermometer;
import com.easternedgerobotics.rov.math.Range;
import com.easternedgerobotics.rov.value.AccelerationValue;
import com.easternedgerobotics.rov.value.AngularVelocityValue;
import com.easternedgerobotics.rov.value.InternalPressureValue;
import com.easternedgerobotics.rov.value.InternalTemperatureValue;
import com.easternedgerobotics.rov.value.RotationValue;

import org.pmw.tinylog.Logger;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;

final class NullChannel implements ADC, PWM {
    private final byte id;

    NullChannel(final byte id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings({"checkstyle:MagicNumber"})
    public final float voltage() {
        return 42;
    }

    @Override
    public final void write(final float value) {
        Logger.debug("Write {} to Maestro channel {}", value, id);
    }

    @Override
    public final void writeZero() {
        write(0);
    }

    @Override
    public final PWM setOutputRange(final Range range) {
        return this;
    }
}

final class NullMaestro extends AbstractList<NullChannel> {
    private static final byte NUMBER_OF_CHANNELS = 24;

    private final Map<Byte, NullChannel> channels = new HashMap<>();

    @Override
    public final NullChannel get(final int index) {
        return channels.computeIfAbsent((byte) index, NullChannel::new);
    }

    @Override
    public final int size() {
        return NUMBER_OF_CHANNELS;
    }
}

class NullAltIMU implements Accelerometer, Barometer, Thermometer, Gyroscope, Magnetometer {
    @Override
    public AccelerationValue acceleration() {
        return new AccelerationValue();
    }

    @Override
    public InternalPressureValue pressure() {
        return new InternalPressureValue();
    }

    @Override
    public AngularVelocityValue angularVelocity() {
        return new AngularVelocityValue();
    }

    @Override
    public RotationValue rotation() {
        return new RotationValue();
    }

    @Override
    public InternalTemperatureValue temperature() {
        return new InternalTemperatureValue();
    }
}

public final class RovSimulator {
    private RovSimulator() {
        // ???
    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final InetAddress broadcastAddress = InetAddress.getByName(System.getProperty("broadcast", "192.168.88.255"));
        final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
        final DatagramSocket socket = new DatagramSocket(broadcastPort);
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
        final Rov rov = new Rov(eventPublisher, new NullMaestro(), new NullAltIMU(), new MockRovConfig());

        Runtime.getRuntime().addShutdownHook(new Thread(rov::shutdown));

        rov.init(Schedulers.io(), Schedulers.computation());
        Logger.info("Started");
        eventPublisher.await();
    }
}
