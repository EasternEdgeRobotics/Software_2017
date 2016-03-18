package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.I2C;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;
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

    private static final long SLEEP_DURATION = 10L;

    private static final String PORT_AFT_NAME = "PortAft";

    private static final String STARBOARD_AFT_NAME = "StarboardAft";

    private static final String PORT_FORE_NAME = "PortFore";

    private static final String STARBOARD_FORE_NAME = "StarboardFore";

    private static final String PORT_VERT_NAME = "PortVert";

    private static final String STARBOARD_VERT_NAME = "StarboardVert";

    private static final byte PORT_AFT_ADDRESS = 0x2A;

    private static final byte STARBOARD_AFT_ADDRESS = 0x2C;

    private static final byte PORT_FORE_ADDRESS = 0x2D;

    private static final byte STARBOARD_FORE_ADDRESS = 0x2E;

    private static final byte PORT_VERT_ADDRESS = 0x2F;

    private static final byte STARBOARD_VERT_ADDRESS = 0x2B;

    private final SixThrusterConfig thrusterConfig;

    private final List<Thruster> thrusters;

    private final EventPublisher eventPublisher;

    private Rov(final EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        try {
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
                new Thruster(eventPublisher, portAft, new I2C(bus.getDevice(PORT_AFT_ADDRESS))),
                new Thruster(eventPublisher, starboardAft, new I2C(bus.getDevice(STARBOARD_AFT_ADDRESS))),
                new Thruster(eventPublisher, portFore, new I2C(bus.getDevice(PORT_FORE_ADDRESS))),
                new Thruster(eventPublisher, starboardFore, new I2C(bus.getDevice(STARBOARD_FORE_ADDRESS))),
                new Thruster(eventPublisher, portVert, new I2C(bus.getDevice(PORT_VERT_ADDRESS))),
                new Thruster(eventPublisher, starboardVert, new I2C(bus.getDevice(STARBOARD_VERT_ADDRESS)))
            ));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        Observable.interval(SLEEP_DURATION, TimeUnit.MILLISECONDS).subscribe(this::thrustersUpdate);
    }

    public void shutdown() {
        Logger.info("Shutting down ROV");
        thrusterConfig.updateZero();
        thrusters.forEach(thruster -> {
            try {
                thruster.writeZero();
            } catch (final IOException ex) {
                Logger.error(ex);
            }
        });
        eventPublisher.stop();
    }

    private void thrustersUpdate(final long tick) {
        thrusterConfig.update();
        thrusters.forEach(thruster -> {
            try {
                thruster.write();
            } catch (final IOException ex) {
                Logger.error(ex);
            }
        });
    }

    public static void main(final String[] args) throws InterruptedException {
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

            eventPublisher.valuesOfType(HeartbeatValue.class)
                .timeout(MAX_HEARTBEAT_GAP, TimeUnit.SECONDS)
                .subscribe(new RovStatusController(rov));

            final float safeAirRatio = 0.1f;
            eventPublisher.emit(MotionPowerValue.create(safeAirRatio, 1, 1, 1, 1, 1, 1));

            Logger.info("Waiting");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}
