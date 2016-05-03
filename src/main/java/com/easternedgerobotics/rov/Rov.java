package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.io.pololu.PololuMaestro;
import com.easternedgerobotics.rov.io.pololu.PololuMaestroChannel;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;
import rx.Observable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class Rov {
    private static final long MAX_HEARTBEAT_GAP = 5;

    private static final long SLEEP_DURATION = 100;

    private static final String PORT_AFT_NAME = "PortAft";

    private static final String STARBOARD_AFT_NAME = "StarboardAft";

    private static final String PORT_FORE_NAME = "PortFore";

    private static final String STARBOARD_FORE_NAME = "StarboardFore";

    private static final String PORT_VERT_NAME = "PortVert";

    private static final String STARBOARD_VERT_NAME = "StarboardVert";

    private static final byte MAESTRO_DEVICE_NUMBER = 0x01;

    private static final byte PORT_AFT_CHANNEL = 14;

    private static final byte STARBOARD_AFT_CHANNEL = 17;

    private static final byte PORT_FORE_CHANNEL = 12;

    private static final byte STARBOARD_FORE_CHANNEL = 15;

    private static final byte PORT_VERT_CHANNEL = 13;

    private static final byte STARBOARD_VERT_CHANNEL = 16;

    private final SixThrusterConfig thrusterConfig;

    private final List<Thruster> thrusters;

    private final EventPublisher eventPublisher;

    private Rov(final EventPublisher eventPublisher, final Serial serial) throws IOException {
        this.eventPublisher = eventPublisher;

        final PololuMaestro maestro = new PololuMaestro(serial, MAESTRO_DEVICE_NUMBER);
        final Observable<ThrusterSpeedValue> thrusterSpeeds = eventPublisher.valuesOfType(ThrusterSpeedValue.class);

        final ThrusterSpeedValue portAft = ThrusterSpeedValue.create(PORT_AFT_NAME);
        final ThrusterSpeedValue starboardAft = ThrusterSpeedValue.create(STARBOARD_AFT_NAME);
        final ThrusterSpeedValue portFore = ThrusterSpeedValue.create(PORT_FORE_NAME);
        final ThrusterSpeedValue starboardFore = ThrusterSpeedValue.create(STARBOARD_FORE_NAME);
        final ThrusterSpeedValue portVert = ThrusterSpeedValue.create(PORT_VERT_NAME);
        final ThrusterSpeedValue starboardVert = ThrusterSpeedValue.create(STARBOARD_VERT_NAME);

        this.thrusterConfig = new SixThrusterConfig(
            eventPublisher,
            portAft,
            starboardAft,
            portFore,
            starboardFore,
            portVert,
            starboardVert
        );

        this.thrusters = Collections.unmodifiableList(Arrays.asList(
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(PORT_AFT_NAME)),
                new PololuMaestroChannel(maestro, PORT_AFT_CHANNEL)),
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(STARBOARD_AFT_NAME)),
                new PololuMaestroChannel(maestro, STARBOARD_AFT_CHANNEL)),
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(PORT_FORE_NAME)),
                new PololuMaestroChannel(maestro, PORT_FORE_CHANNEL)),
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(STARBOARD_FORE_NAME)),
                new PololuMaestroChannel(maestro, STARBOARD_FORE_CHANNEL)),
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(PORT_VERT_NAME)),
                new PololuMaestroChannel(maestro, PORT_VERT_CHANNEL)),
            new Thruster(
                thrusterSpeeds.filter(x -> x.getName().equals(STARBOARD_VERT_NAME)),
                new PololuMaestroChannel(maestro, STARBOARD_VERT_CHANNEL))
        ));
    }

    /**
     * Initialises the ROV, attaching the hardware updates to their event source. The ROV will "timeout"
     * if communication with the topside is lost or the received heartbeat value indicates a non-operational
     * status and will shutdown.
     */
    private void init() {
        Logger.debug("Wiring up heartbeat, timeout, and thruster updates");
        final Observable<HeartbeatValue> timeout = Observable.just(HeartbeatValue.create(false))
            .delay(MAX_HEARTBEAT_GAP, TimeUnit.SECONDS)
            .doOnNext(heartbeat -> Logger.warn("Timeout while waiting for heartbeat"))
            .concatWith(Observable.never());

        final Observable<HeartbeatValue> heartbeats = eventPublisher.valuesOfType(HeartbeatValue.class);

        thrusters.forEach(Thruster::writeZero);

        Observable.interval(SLEEP_DURATION, TimeUnit.MILLISECONDS)
            .withLatestFrom(
                heartbeats.mergeWith(timeout.takeUntil(heartbeats).repeat()), (tick, heartbeat) -> heartbeat)
            .subscribe(this::beat, RuntimeException::new);
    }

    private void thrustersUpdate() {
        thrusterConfig.update();
        thrusters.forEach(Thruster::write);
    }

    private void softShutdown() {
        thrusterConfig.updateZero();
        thrusters.forEach(Thruster::writeZero);
    }

    private void beat(final HeartbeatValue heartbeat) {
        if (heartbeat.isOperational()) {
            thrustersUpdate();
        } else {
            softShutdown();
        }
    }

    public static void main(final String[] args) throws InterruptedException, IOException {
        final String app = "rov";
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
            .desc("the baud rate to use")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(broadcast);
        options.addOption(serialPort);
        options.addOption(baudRate);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final Serial serial = SerialFactory.createInstance();
            final EventPublisher eventPublisher = new UdpEventPublisher(arguments.getOptionValue("b"));
            final Rov rov = new Rov(eventPublisher, serial);

            serial.open(arguments.getOptionValue("s"), Integer.parseInt(arguments.getOptionValue("r")));
            rov.init();

            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
