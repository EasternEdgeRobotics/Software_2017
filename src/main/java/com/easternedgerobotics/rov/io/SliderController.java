package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.control.AnalogToPowerLevel;
import com.easternedgerobotics.rov.control.SupressObservable;
import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.LightSpeedValue;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import rx.Observable;
import rx.Scheduler;

public final class SliderController {
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

    private final Observable<MotionPowerValue> motion;

    private final Observable<LightSpeedValue> lights;

    /**
     * Create a pilot panel instance for the Arduino Mega on the given com port.
     *
     * @param arduino the device to get the sliders from.
     */
    public SliderController(
        final Arduino arduino,
        final Scheduler scheduler,
        @Event final Observable<MotionPowerValue> motionPowerValues
    ) {
        // Get an observable for each of the power sliders
        final Observable<Float> global = arduino.analogPin(GLOBAL_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> heave = arduino.analogPin(HEAVE_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> sway = arduino.analogPin(SWAY_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> surge = arduino.analogPin(SURGE_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> pitch = Observable.just(0f).map(AnalogToPowerLevel::convert);
        final Observable<Float> yaw = arduino.analogPin(YAW_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> roll = arduino.analogPin(ROLL_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);

        final SupressObservable<MotionPowerValue> external = new SupressObservable<>(
            motionPowerValues, scheduler, 2000);

        // Keep track of the latest values in each component
        final Observable<MotionPowerValue> combined = Observable.combineLatest(
            global.mergeWith(external.get().map(MotionPowerValue::getGlobal)),
            heave.mergeWith(external.get().map(MotionPowerValue::getHeave)),
            sway.mergeWith(external.get().map(MotionPowerValue::getSway)),
            surge.mergeWith(external.get().map(MotionPowerValue::getSurge)),
            pitch.mergeWith(external.get().map(MotionPowerValue::getPitch)),
            yaw.mergeWith(external.get().map(MotionPowerValue::getYaw)),
            roll.mergeWith(external.get().map(MotionPowerValue::getRoll)),
            MotionPowerValue::new);

        // Only trigger when sliders are moved
        motion = Observable.merge(global, heave, sway, surge, pitch, yaw, roll)
            .withLatestFrom(combined, (trigger, latest) -> latest)
            .doOnEach(val -> external.supress())
            .share();

        lights = arduino.analogPin(LIGHT_POWER_SLIDER_ADDRESS)
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert)
            .map(LightSpeedValue::new)
            .share();
    }

    public Observable<MotionPowerValue> getMotion() {
        return motion;
    }

    public Observable<LightSpeedValue> getLights() {
        return lights;
    }
}
