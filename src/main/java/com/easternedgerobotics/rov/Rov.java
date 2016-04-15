package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.ThrusterSpeedValue;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
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

    private static final byte PORT_AFT_ADDRESS = 0x2A;

    private static final byte STARBOARD_AFT_ADDRESS = 0x2C;

    private static final byte PORT_FORE_ADDRESS = 0x31;

    private static final byte STARBOARD_FORE_ADDRESS = 0x2E;

    private static final byte PORT_VERT_ADDRESS = 0x2F;

    private static final byte STARBOARD_VERT_ADDRESS = 0x30;

    private final SixThrusterConfig thrusterConfig;

    private final List<Thruster> thrusters;

    private final EventPublisher eventPublisher;

    private Rov(final EventPublisher eventPublisher) throws IOException {
        this.eventPublisher = eventPublisher;

        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

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
            new Thruster(eventPublisher, portAft, bus.getDevice(PORT_AFT_ADDRESS)),
            new Thruster(eventPublisher, starboardAft, bus.getDevice(STARBOARD_AFT_ADDRESS)),
            new Thruster(eventPublisher, portFore, bus.getDevice(PORT_FORE_ADDRESS)),
            new Thruster(eventPublisher, starboardFore, bus.getDevice(STARBOARD_FORE_ADDRESS)),
            new Thruster(eventPublisher, portVert, bus.getDevice(PORT_VERT_ADDRESS)),
            new Thruster(eventPublisher, starboardVert, bus.getDevice(STARBOARD_VERT_ADDRESS))
        ));
    }

    /**
     * Initialises the ROV, attaching the hardware updates to their event source. The ROV will "timeout"
     * if communication with the topside is lost or the received heartbeat value indicates a non-operational
     * status and will shutdown.
     */
    private void init() {
        final Observable<HeartbeatValue> timeout = Observable.just(HeartbeatValue.create(false))
            .delay(MAX_HEARTBEAT_GAP, TimeUnit.SECONDS)
            .concatWith(Observable.never());

        final Observable<HeartbeatValue> heartbeats = eventPublisher.valuesOfType(HeartbeatValue.class);

        Observable.interval(SLEEP_DURATION, TimeUnit.MILLISECONDS)
            .withLatestFrom(
                heartbeats.mergeWith(timeout.takeUntil(heartbeats).repeat()), (tick, heartbeat) -> heartbeat)
            .subscribe(this::beat, RuntimeException::new);
    }

    private void thrustersUpdate() {
        thrusterConfig.update();
        thrusters.forEach(thruster -> {
            try {
                thruster.write();
            } catch (final IOException ex) {
                Logger.debug(ex);
            }
        });
    }

    private void softShutdown() {
        thrusterConfig.updateZero();
        thrusters.forEach(thruster -> {
            try {
                thruster.writeZero();
            } catch (final IOException ex) {
                Logger.debug(ex);
            }
        });
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

        final Options options = new Options();
        options.addOption(broadcast);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final EventPublisher eventPublisher = new UdpEventPublisher(arguments.getOptionValue("b"));
            final Rov rov = new Rov(eventPublisher);

            rov.init();

            Logger.info("Started");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
