package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.control.AnalogToPowerLevel;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.io.arduino.ArduinoPort;
import com.easternedgerobotics.rov.value.AnalogPinValue;

import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PilotPanel {
    /**
     * The number of light buttons on the pilot panel.
     */
    public static final int LIGHT_BUTTON_COUNT = 9;

    /**
     * Max tim to wait for initial com port connection.
     */
    private static final int ARDUINO_TIMEOUT = 2000;

    /**
     * Baud rate for the pilot panel Arduin Mega.
     */
    private static final int ARDUINO_BAUD = 115200;

    /**
     * Index of inputs on the pilot panel.
     */
    private static final byte[] ARDUINO_INPUTS = {};

    /**
     * Index of input pullups on the pilot panel.
     */
    private static final byte[] ARDUINO_INPUT_PULLUPS = {22, 24, 26, 28, 30, 32, 34, 36, 38, 53};

    /**
     * Index of outputs on the pilot panel.
     */
    private static final byte[] ARDUINO_OUTPUTS = {23, 25, 27, 29, 31, 33, 35, 37, 39};

    /**
     * Rate at which heartbeats are sent to the Arduino.
     */
    private static final int ARDUINO_HEARTBEAT_INTERVAL = 500;

    /**
     * Time between heartbeats which the Arduino is considered dead.
     */
    private static final int ARDUINO_HEARTBEAT_TIMEOUT = 5000;

    /**
     * Index of the global power slider.
     */
    private static final byte GLOBAL_POWER_SLIDER_ADDRESS = 3;

    /**
     * Index of the heave power slider.
     */
    private static final byte HEAVE_POWER_SLIDER_ADDRESS = 2;

    /**
     * Index of the sway power slider.
     */
    private static final byte SWAY_POWER_SLIDER_ADDRESS = 1;

    /**
     * Index of the surge power slider.
     */
    private static final byte SURGE_POWER_SLIDER_ADDRESS = 0;

    /**
     * Index of the pitch power slider.
     */
    private static final byte PITCH_POWER_SLIDER_ADDRESS = -1;

    /**
     * Index of the yaw power slider.
     */
    private static final byte YAW_POWER_SLIDER_ADDRESS = 5;

    /**
     * Index of the roll power slider.
     */
    private static final byte ROLL_POWER_SLIDER_ADDRESS = 4;

    /**
     * Index of the light power slider.
     */
    private static final byte LIGHT_POWER_SLIDER_ADDRESS = 7;

    /**
     * Address of the emergency stop buttons.
     */
    private static final byte EMERGENCY_STOP_BUTTON_ADDRESS = 53;

    /**
     * The Arduino Mega instance.
     */
    private final Arduino arduino;

    /**
     * PilotButton and LED pairs on the buttons panel unit.
     */
    private final List<PilotButton> buttons;

    /**
     * PilotButton for the emergency stop.
     */
    private final PilotButton emergencyStop;

    /**
     * Create a pilot panel instance for the Arduino Mega on the given com port.
     *
     * @param arduinoComName unique name for the RXTX serial port.
     * @param arduinoCom the com port address for the arduino.
     */
    public PilotPanel(final String arduinoComName, final String arduinoCom) {
        arduino = new Arduino(new ArduinoPort(arduinoComName, arduinoCom, ARDUINO_TIMEOUT, ARDUINO_BAUD),
            ARDUINO_INPUTS, ARDUINO_OUTPUTS, ARDUINO_INPUT_PULLUPS);

        buttons = Collections.unmodifiableList(new ArrayList<PilotButton>() {
            {
                for (int i = 0; i < LIGHT_BUTTON_COUNT; i++) {
                    final PilotButton button = new PilotButton(arduino, ARDUINO_INPUT_PULLUPS[i], ARDUINO_OUTPUTS[i]);
                    button.click().map(x -> !x).subscribe(button::setLight);
                    add(button);
                }
            }
        });
        emergencyStop = new PilotButton(arduino, EMERGENCY_STOP_BUTTON_ADDRESS);
    }

    /**
     * Start communications with the Arduino.
     */
    public void start() {
        arduino.start(ARDUINO_HEARTBEAT_INTERVAL, ARDUINO_HEARTBEAT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * End communications with the Arduino.
     */
    public void stop() {
        arduino.stop();
    }

    /**
     * Observe changes on the global power slider.
     *
     * @return observable
     */
    public Observable<Integer> gloablPowerSlider() {
        return arduino.analogPin(GLOBAL_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the heave power slider.
     *
     * @return observable
     */
    public Observable<Integer> heavePowerSlider() {
        return arduino.analogPin(HEAVE_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the sway power slider.
     *
     * @return observable
     */
    public Observable<Integer> swayPowerSlider() {
        return arduino.analogPin(SWAY_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the surge power slider.
     *
     * @return observable
     */
    public Observable<Integer> surgePowerSlider() {
        return arduino.analogPin(SURGE_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the pitch power slider.
     *
     * @return observable
     */
    public Observable<Integer> pitchPowerSlider() {
        return arduino.analogPin(PITCH_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the yaw power slider.
     *
     * @return observable
     */
    public Observable<Integer> yawPowerSlider() {
        return arduino.analogPin(YAW_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the roll power slider.
     *
     * @return observable
     */
    public Observable<Integer> rollPowerSlider() {
        return arduino.analogPin(ROLL_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the light power slider.
     *
     * @return observable
     */
    public Observable<Integer> lightPowerSlider() {
        return arduino.analogPin(LIGHT_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue)
            .map(AnalogToPowerLevel::convert);
    }

    /**
     * Observe changes on the emergency stop buttons.
     *
     * @return observable
     */
    public Observable<Boolean> emergencyStopClick() {
        return emergencyStop.click();
    }

    /**
     * Observe normal buttons presses on the pilot panel.
     *
     * @param index the buttons index
     * @return observable
     */
    public Observable<Boolean> click(final int index) {
        return buttons.get(index).click();
    }

    /**
     * Set the LED state of a particular button.
     *
     * @param index the button index
     * @param value the new LED state
     */
    public void setLight(final int index, final boolean value) {
        buttons.get(index).setLight(value);
    }
}
