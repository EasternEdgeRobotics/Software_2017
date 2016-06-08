package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoFlipValueA;
import com.easternedgerobotics.rov.value.VideoValueA;
import com.easternedgerobotics.rov.video.PicameraVideo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.subscriptions.CompositeSubscription;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

final class PicameraA {
    private final EventPublisher eventPublisher;

    private final CompositeSubscription subscriptions;

    private PicameraA(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.subscriptions = new CompositeSubscription();
    }

    private void initCameraA() {
        Logger.info("Initialising video camera A");
        subscriptions.add(eventPublisher.valuesOfType(VideoValueA.class)
            .first()
            .delay(1, TimeUnit.SECONDS)
            .subscribe(this::startCameraA));
    }

    private void startCameraA(final VideoValueA value) {
        final PicameraVideo video = new PicameraVideo(value.getHost(), value.getPort());

        Logger.info("Starting video camera A");
        video.start();
        subscriptions.add(eventPublisher.valuesOfType(VideoFlipValueA.class)
            .subscribe(f -> video.flip()));
    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final String app = "picamera-a";
        final HelpFormatter formatter = new HelpFormatter();
        final Option broadcast = Option.builder("b")
            .longOpt("broadcast")
            .hasArg()
            .argName("ADDRESS")
            .desc("use ADDRESS to broadcast messages")
            .required()
            .build();
        final Option serialPort = Option.builder("s")
            .longOpt("serial-port")
            .hasArg()
            .argName("FILE")
            .desc("read and write to FILE as serial device")
            .required()
            .build();
        final Option baudRate = Option.builder("r")
            .type(Integer.class)
            .longOpt("baud-rate")
            .hasArg()
            .argName("BPS")
            .desc("the serial baud rate to use")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(broadcast);
        options.addOption(serialPort);
        options.addOption(baudRate);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final InetAddress broadcastAddress = InetAddress.getByName(arguments.getOptionValue("b"));
            final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
            final DatagramSocket socket = new DatagramSocket(broadcastPort);
            final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
            final PicameraA picamera = new PicameraA(eventPublisher);

            picamera.initCameraA();
            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
