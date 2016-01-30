package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.event.io.KryoSerializer;
import com.easternedgerobotics.rov.io.Thruster;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.ThrusterValue;

import java.io.IOException;

/**
 * Model for testing thruster control. Creates six thrusters and puts them in the sixThrusterConfig.
 */
final class ThrusterTestModel implements TestModel {

    // Rov configuration

    public static final String PORT_AFT_NAME = "PortAft";

    public static final String STARBOARD_AFT_NAME = "StarboardAft";

    public static final String PORT_FORE_NAME = "PortFore";

    public static final String STARBOARD_FORE_NAME = "StarboardFore";

    public static final String PORT_VERT_NAME = "PortVert";

    public static final String STARBOARD_VERT_NAME = "StarboardVert";

    public static final byte PORT_AFT_ADDRESS = 0x29;

    public static final byte STARBOARD_AFT_ADDRESS = 0x2A;

    public static final byte PORT_FORE_ADDRESS = 0x2B;

    public static final byte STARBOARD_FORE_ADDRESS = 0x2C;

    public static final byte PORT_VERT_ADDRESS = 0x2D;

    public static final byte STARBOARD_VERT_ADDRESS = 0x2E;

    private I2CSim portAftDevice;

    private I2CSim portForeDevice;

    private I2CSim portVertDevice;

    private I2CSim stbdAftDevice;

    private I2CSim stbdForeDevice;

    private I2CSim stbdVertDevice;

    private EventPublisher eventPublisher;

    private SixThrusterConfig thrusterConfig;

    private Thruster[] thrusters;

    public ThrusterTestModel(final String broadcast, final int port) {
        try {
            // Set up event publisher
            this.eventPublisher = new UdpEventPublisher(new KryoSerializer(), port, broadcast, port);

            // Create initial thruster values
            final ThrusterValue portAft =       ThrusterValue.create(PORT_AFT_NAME);
            final ThrusterValue starboardAft =  ThrusterValue.create(STARBOARD_AFT_NAME);
            final ThrusterValue portFore =      ThrusterValue.create(PORT_FORE_NAME);
            final ThrusterValue starboardFore = ThrusterValue.create(STARBOARD_FORE_NAME);
            final ThrusterValue portVert =      ThrusterValue.create(PORT_VERT_NAME);
            final ThrusterValue starboardVert = ThrusterValue.create(STARBOARD_VERT_NAME);

            // Create control which creates next-step thruster values
            thrusterConfig = new SixThrusterConfig(eventPublisher,
                portAft,
                starboardAft,
                portFore,
                starboardFore,
                portVert,
                starboardVert
            );

            portAftDevice = new I2CSim(PORT_AFT_ADDRESS);
            portForeDevice = new I2CSim(PORT_FORE_ADDRESS);
            portVertDevice = new I2CSim(PORT_VERT_ADDRESS);
            stbdAftDevice = new I2CSim(STARBOARD_AFT_ADDRESS);
            stbdForeDevice = new I2CSim(STARBOARD_FORE_ADDRESS);
            stbdVertDevice = new I2CSim(STARBOARD_VERT_ADDRESS);

            thrusters = new Thruster[] {
                new Thruster(eventPublisher, portAft,       portAftDevice),
                new Thruster(eventPublisher, starboardAft,  stbdAftDevice),
                new Thruster(eventPublisher, portFore,      portForeDevice),
                new Thruster(eventPublisher, starboardFore, stbdForeDevice),
                new Thruster(eventPublisher, portVert,      portVertDevice),
                new Thruster(eventPublisher, starboardVert, stbdVertDevice),
            };

            // Set all power levels to 1 so motion values are unaffected
            eventPublisher.emit(MotionPowerValue.create(1, 1, 1, 1, 1, 1, 1));

        } catch (final UnsatisfiedLinkError | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    @Override
    public void update() {
        try {
            thrusterConfig.update();
            for (Thruster thruster : thrusters) {
                thruster.write();
                thruster.read();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateZero() {
        try {
            thrusterConfig.updateZero();
            for (Thruster thruster : thrusters) {
                thruster.writeZero();
                thruster.read();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public I2CSim getI2CSim(final String name) {
        I2CSim d = null;
        if (name == ThrusterTestModel.PORT_AFT_NAME) {
            d = portAftDevice;
        } else if (name == ThrusterTestModel.PORT_FORE_NAME) {
            d = portForeDevice;
        } else if (name == ThrusterTestModel.PORT_VERT_NAME) {
            d = portVertDevice;
        } else if (name == ThrusterTestModel.STARBOARD_AFT_NAME) {
            d = stbdAftDevice;
        } else if (name == ThrusterTestModel.STARBOARD_FORE_NAME) {
            d = stbdForeDevice;
        } else if (name == ThrusterTestModel.STARBOARD_VERT_NAME) {
            d = stbdVertDevice;
        }
        return d;
    }

}
