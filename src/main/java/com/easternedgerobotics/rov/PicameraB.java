package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.VideoFlipValueB;
import com.easternedgerobotics.rov.value.VideoValueB;
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

final class PicameraB {
    private final EventPublisher eventPublisher;

    private final CompositeSubscription subscriptions;

    private final CompositeSubscription flips;

    private PicameraB(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.subscriptions = new CompositeSubscription();
        this.flips = new CompositeSubscription();
    }

    private void initCameraB() {
        subscriptions.add(
            eventPublisher.valuesOfType(VideoValueB.class)
                .map(value -> new PicameraVideo(value.getHost(), value.getPort()))
                .scan((old, event) -> {
                    flips.clear();
                    old.stop();
                    return event;
                })
                .delay(1, TimeUnit.SECONDS)
                .subscribe(video -> {
                    video.start();
                    flips.add(eventPublisher.valuesOfType(VideoFlipValueB.class).subscribe(f -> video.flip()));
                }));
    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final String app = "picamera-b";
        final HelpFormatter formatter = new HelpFormatter();
        final Option broadcast = Option.builder("b")
            .longOpt("broadcast")
            .hasArg()
            .argName("ADDRESS")
            .desc("use ADDRESS to broadcast messages")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(broadcast);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final InetAddress broadcastAddress = InetAddress.getByName(arguments.getOptionValue("b"));
            final int broadcastPort = BroadcastEventPublisher.DEFAULT_BROADCAST_PORT;
            final DatagramSocket socket = new DatagramSocket(broadcastPort);
            final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
            final PicameraB picamera = new PicameraB(eventPublisher);

            picamera.initCameraB();
            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
