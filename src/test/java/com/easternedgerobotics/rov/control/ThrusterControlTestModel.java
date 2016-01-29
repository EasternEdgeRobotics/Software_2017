package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.event.io.KryoSerializer;
import com.easternedgerobotics.rov.value.MotionPowerValue;
import com.easternedgerobotics.rov.value.ThrusterValue;

/* ******
 * Model for testing thruster control. Creates six thrusters and puts them in the sixThrusterConfig.
 */
final class ThrusterControlTestModel implements TestModel {

    // Rov configuration

    public static final String PORT_AFT_NAME = "PortAft";

    public static final String STARBOARD_AFT_NAME = "StarboardAft";

    public static final String PORT_FORE_NAME = "PortFore";

    public static final String STARBOARD_FORE_NAME = "StarboardFore";

    public static final String PORT_VERT_NAME = "PortVert";

    public static final String STARBOARD_VERT_NAME = "StarboardVert";
    
    private ThrusterValue portAft;
    
    private ThrusterValue starboardAft;
    
    private ThrusterValue portFore;
    
    private ThrusterValue starboardFore;
    
    private ThrusterValue portVert;
    
    private ThrusterValue starboardVert;
    
    private EventPublisher eventPublisher;
    
    private SixThrusterConfig thrusterConfig;

    public ThrusterControlTestModel(final String broadcast, final int port) {
        try {
            // Set up event publisher
            this.eventPublisher = new UdpEventPublisher(new KryoSerializer(), port, broadcast, port);

            // Create initial thruster values
            this.portAft =       ThrusterValue.create(PORT_AFT_NAME);
            this.starboardAft =  ThrusterValue.create(STARBOARD_AFT_NAME);
            this.portFore =      ThrusterValue.create(PORT_FORE_NAME);
            this.starboardFore = ThrusterValue.create(STARBOARD_FORE_NAME);
            this.portVert =      ThrusterValue.create(PORT_VERT_NAME);
            this.starboardVert = ThrusterValue.create(STARBOARD_VERT_NAME);

            // Create control which creates next-step thruster values
            thrusterConfig = new SixThrusterConfig(eventPublisher,
                portAft,
                starboardAft,
                portFore,
                starboardFore,
                portVert,
                starboardVert
            );
            
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
        thrusterConfig.update();
    }

    @Override
    public void updateZero() {
        thrusterConfig.updateZero();
    }

}
