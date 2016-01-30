package com.easternedgerobotics.rov.control;

import org.junit.Before;

/* ***
 * Tests for thruster output.
 * Inputs motion values (like joystick input) and looks for the correct changes to device output.
 * Uses I2CSim objects to simulate sending bytes to a thruster.
 */
public class ThrusterTest {

    private final int port = 8000;

    private final double precision = 0.0001;

    private final int maxForward = 127;

    private final int maxReverse = -128;

    private final int sleepTime1 = 3000;

    private final int sleepTime2 = 1000;

    private ThrusterTestModel model;

    private I2CSim portFore;

    private I2CSim portAft;

    private I2CSim portVert;

    private I2CSim stbdFore;

    private I2CSim stbdAft;

    private I2CSim stbdVert;

    @Before
    public final void setup() {
        model = new ThrusterTestModel("localhost", port);
        portFore = model.getI2CSim(ThrusterControlTestModel.PORT_FORE_NAME);
        portAft = model.getI2CSim(ThrusterControlTestModel.PORT_AFT_NAME);
        portVert = model.getI2CSim(ThrusterControlTestModel.PORT_VERT_NAME);
        stbdFore = model.getI2CSim(ThrusterControlTestModel.STARBOARD_FORE_NAME);
        stbdAft = model.getI2CSim(ThrusterControlTestModel.STARBOARD_AFT_NAME);
        stbdVert = model.getI2CSim(ThrusterControlTestModel.STARBOARD_VERT_NAME);
        model.update();
    }
}
