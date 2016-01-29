package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.control.SixThrusterConfig;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.event.io.KryoSerializer;
import com.easternedgerobotics.rov.io.I2C;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.value.HeartbeatValue;
import com.easternedgerobotics.rov.value.ThrusterValue;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import rx.Observable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

final class Rov {

    // Rov configuration
    public static final long SLEEP_DURATION = 10L;

    private static final long MAX_HEARTBEAT_GAP = 500L;

    public static final String PORT_AFT_NAME = "PortAft";

    public static final String STARBOARD_AFT_NAME = "StarboardAft";

    public static final String PORT_FORE_NAME = "PortFore";

    public static final String STARBORAD_FORE_NAME = "StarboardFore";

    public static final String PORT_VERT_NAME = "PortVert";

    public static final String STARBOARD_VERT_NAME = "StarboardVert";

    public static final byte PORT_AFT_ADDRESS = 0x29;

    public static final byte STARBOARD_AFT_ADDRESS = 0x2A;

    public static final byte PORT_FORE_ADDRESS = 0x2B;

    public static final byte STARBORAD_FORE_ADDRESS = 0x2C;

    public static final byte PORT_VERT_ADDRESS = 0x2D;

    public static final byte STARBOARD_VERT_ADDRESS = 0x2E;

    // Client connection state
    private boolean isOperational = false;

    private long lastClientUpdate = System.currentTimeMillis();

    private Rov(final String broadcast, final int port) {
        try {
            // Set up event publisher
            final EventPublisher eventPublisher = new UdpEventPublisher(new KryoSerializer(), port, broadcast, port);

            // Create initial thruster values
            final ThrusterValue portAft =       ThrusterValue.create(PORT_AFT_NAME);
            final ThrusterValue starboardAft =  ThrusterValue.create(STARBOARD_AFT_NAME);
            final ThrusterValue portFore =      ThrusterValue.create(PORT_FORE_NAME);
            final ThrusterValue starboardFore = ThrusterValue.create(STARBORAD_FORE_NAME);
            final ThrusterValue portVert =      ThrusterValue.create(PORT_VERT_NAME);
            final ThrusterValue starboardVert = ThrusterValue.create(STARBOARD_VERT_NAME);

            // Create control which creates next-step thruster values
            final SixThrusterConfig thrusterConfig = new SixThrusterConfig(eventPublisher,
                portAft,
                starboardAft,
                portFore,
                starboardFore,
                portVert,
                starboardVert
            );

            // Create devices which sends thruster values
            final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

            final Thruster[] thrusters =  {
                new Thruster(eventPublisher, portAft,       new I2C(bus.getDevice(PORT_AFT_ADDRESS))),
                new Thruster(eventPublisher, starboardAft,  new I2C(bus.getDevice(STARBOARD_AFT_ADDRESS))),
                new Thruster(eventPublisher, portFore,      new I2C(bus.getDevice(PORT_FORE_ADDRESS))),
                new Thruster(eventPublisher, starboardFore, new I2C(bus.getDevice(STARBORAD_FORE_ADDRESS))),
                new Thruster(eventPublisher, portVert,      new I2C(bus.getDevice(PORT_VERT_ADDRESS))),
                new Thruster(eventPublisher, starboardVert, new I2C(bus.getDevice(STARBOARD_VERT_ADDRESS))),
            };

            // Subscribe to the client heartbeats
            eventPublisher.valuesOfType(HeartbeatValue.class).subscribe(heartbeatValue -> {
                isOperational = heartbeatValue.isOperational();
                lastClientUpdate = System.currentTimeMillis();
            });

            // thread to update controls and devices with
            final Observable<Boolean> operationalObserver = Observable.interval(SLEEP_DURATION, TimeUnit.MILLISECONDS)
                .map(l -> (System.currentTimeMillis() - lastClientUpdate > MAX_HEARTBEAT_GAP) && isOperational);

            // define on and off conditions for the rov
            operationalObserver.subscribe(operational -> {
                try {
                    if (operational) {
                        thrusterConfig.update();
                        for (Thruster thruster : thrusters) {
                            thruster.write();
                            thruster.read();
                        }
                    } else {
                        thrusterConfig.updateZero();
                        for (Thruster thruster : thrusters) {
                            thruster.writeZero();
                            thruster.read();
                        }
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });

            // Stop program from exiting
            System.out.println("Press enter to quit");
            System.in.read();

        } catch (final UnsatisfiedLinkError | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
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
        final Option port = Option.builder("p")
            .longOpt("port")
            .hasArg()
            .argName("PORT")
            .type(Integer.class)
            .desc("listen on PORT for broadcast messages")
            .build();
        final Option help = Option.builder()
            .longOpt("help")
            .desc("display this help text and exit")
            .build();

        final Options options = new Options();
        options.addOption(broadcast);
        options.addOption(port);
        options.addOption(help);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);
            if (arguments.hasOption("help")) {
                formatter.printHelp(app, options, true);
                System.exit(0);
            }

            new Rov(arguments.getOptionValue("b"), Integer.parseInt(arguments.getOptionValue("p")));

        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }
}
