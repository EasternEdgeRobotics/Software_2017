package com.easternedgerobotics.rov.control;

import org.junit.Before;

/**
 * Tests for thruster control. Inputs motion values (like joystick input) and looks for
 * the correct changes to thruster values.
 *
 * <p>
 * Since it only observes thruster values, these tests don't go into the i2c interaction.
 */
public class ThrusterControlTest {
    private final int port = 8001;

    private final double precision = 0.0001;

    private final int firstSleepDuration = 3000;

    private final int secondSleepDuration = 1000;

    private ThrusterControlTestModel model;

    private ThrusterValueListener portFore;

    private ThrusterValueListener portAft;

    private ThrusterValueListener portVert;

    private ThrusterValueListener stbdFore;

    private ThrusterValueListener stbdAft;

    private ThrusterValueListener stbdVert;

    @Before
    public final void setup() {
        model = new ThrusterControlTestModel("localhost", port);
        portFore = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_FORE_NAME);
        portAft = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_AFT_NAME);
        portVert = new ThrusterValueListener(model, ThrusterControlTestModel.PORT_VERT_NAME);
        stbdFore = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_FORE_NAME);
        stbdAft = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_AFT_NAME);
        stbdVert = new ThrusterValueListener(model, ThrusterControlTestModel.STARBOARD_VERT_NAME);
        model.update();
    }
}
