package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.config.Config;
import com.easternedgerobotics.rov.config.LaunchConfig;
import com.easternedgerobotics.rov.event.BroadcastEventPublisher;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.rpi.RaspberryCpuInformation;
import com.easternedgerobotics.rov.value.CameraCaptureValueA;
import com.easternedgerobotics.rov.value.PicameraACpuValue;
import com.easternedgerobotics.rov.value.PicameraAHeartbeatValue;
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
import rx.Observable;
import rx.broadcast.BasicOrder;
import rx.broadcast.UdpBroadcast;
import rx.schedulers.Schedulers;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

final class PicameraA {
    private PicameraA() {

    }

    private static void initCameraA(
        final EventPublisher eventPublisher,
        final int heartbeatRate,
        final int fileReceiverPort
    ) {
        eventPublisher.valuesOfType(VideoFlipValueA.class)
            .observeOn(Schedulers.io())
            .subscribe(f -> PicameraVideo.flip());

        eventPublisher.valuesOfType(CameraCaptureValueA.class)
            .observeOn(Schedulers.io())
            .subscribe(capture -> PicameraVideo.takeImage(capture.getPath()));

        eventPublisher.valuesOfType(VideoValueA.class)
            .throttleLast(1, TimeUnit.SECONDS, Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(value -> PicameraVideo.start(value.getHost(), value.getPort(), fileReceiverPort));

        Observable.interval(heartbeatRate, TimeUnit.SECONDS, Schedulers.io())
            .map(t -> new PicameraAHeartbeatValue(true))
            .observeOn(Schedulers.io())
            .subscribe(eventPublisher::emit);

        final RaspberryCpuInformation cpuInformation = new RaspberryCpuInformation(
            PicameraACpuValue::new, heartbeatRate, TimeUnit.SECONDS);
        cpuInformation.observe().subscribe(eventPublisher::emit, Logger::error);
    }

    public static void main(final String[] args) throws InterruptedException, SocketException, UnknownHostException {
        final String app = "picamera-a";
        final HelpFormatter formatter = new HelpFormatter();
        final Option defaultConfig = Option.builder("d")
            .longOpt("default")
            .hasArg()
            .argName("DEFAULT")
            .desc("name of the default config file")
            .required()
            .build();
        final Option config = Option.builder("c")
            .longOpt("config")
            .hasArg()
            .argName("CONFIG")
            .desc("name of the overriding config file")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(defaultConfig);
        options.addOption(config);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final LaunchConfig launchConfig = new Config(
                arguments.getOptionValue("d"),
                arguments.getOptionValue("c")
            ).getConfig("launch", LaunchConfig.class);

            final InetAddress broadcastAddress = InetAddress.getByName(launchConfig.broadcast());
            final int broadcastPort = launchConfig.defaultBroadcastPort();
            final DatagramSocket socket = new DatagramSocket(broadcastPort);
            final EventPublisher eventPublisher = new BroadcastEventPublisher(new UdpBroadcast<>(
                socket, broadcastAddress, broadcastPort, new BasicOrder<>()));
            PicameraVideo.addShutdownHook();
            initCameraA(eventPublisher, launchConfig.heartbeatRate(), launchConfig.fileReceiverPort());
            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
