package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public final class Topside {
    private Topside() {

    }

    private static final long HEARTBEAT_GAP = 100;

    public static void main(final String[] args) throws InterruptedException {
        final String app = "topside";
        final HelpFormatter formatter = new HelpFormatter();
        final Option broadcast = Option.builder("b")
            .longOpt("broadcast")
            .hasArg()
            .argName("ADDRESS")
            .desc("use ADDRESS to broadcast messages")
            .required()
            .build();
        final Option help = Option.builder()
            .longOpt("help")
            .desc("display this help text and exit")
            .build();

        final Options options = new Options();
        options.addOption(broadcast);
        options.addOption(help);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);
            if (arguments.hasOption("help")) {
                formatter.printHelp(app, options, true);
                System.exit(0);
            }

            final EventPublisher eventPublisher = new UdpEventPublisher(arguments.getOptionValue("b"));
            final HeartbeatController heartbeatController = new HeartbeatController(
                eventPublisher, Observable.interval(HEARTBEAT_GAP, TimeUnit.MILLISECONDS));
            final Observable<Joystick> joystick = Joysticks.logitechExtreme3dPro();
            joystick.flatMap(Joystick::axes).subscribe(eventPublisher::emit);

            heartbeatController.start();

            Logger.info("Waiting");
            eventPublisher.await();
        } catch (final ParseException e) {
            Logger.error(e);
        }
    }
}
