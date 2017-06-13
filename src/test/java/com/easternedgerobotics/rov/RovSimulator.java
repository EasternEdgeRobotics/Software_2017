package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.config.MockLaunchConfig;
import com.easternedgerobotics.rov.config.MockRovConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.MockPressureSensor;
import com.easternedgerobotics.rov.io.devices.MockBluetooth;
import com.easternedgerobotics.rov.io.pololu.MockAltIMU;
import com.easternedgerobotics.rov.io.pololu.MockMaestro;

import org.pmw.tinylog.Logger;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public final class RovSimulator {
    private RovSimulator() {
        // ???
    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final LaunchConfig launchConfig = new MockLaunchConfig();
        final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
        final int broadcastPort = launchConfig.defaultBroadcastPort();
        final DatagramSocket socket = new DatagramSocket(broadcastPort);
        final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
            socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
        final MockMaestro maestro = new MockMaestro();
        final MockAltIMU imu = new MockAltIMU();
        final MockPressureSensor pressureSensor = new MockPressureSensor();
        final MockBluetooth bluetooth = new MockBluetooth();
        final MockRovConfig config = new MockRovConfig();
        final Rov rov = new Rov(eventPublisher, maestro, imu, pressureSensor, bluetooth, config);

        Runtime.getRuntime().addShutdownHook(new Thread(rov::shutdown));

        rov.init(Schedulers.io(), Schedulers.newThread(), Schedulers.computation());
        Logger.info("Started");
        eventPublisher.await();
    }
}
