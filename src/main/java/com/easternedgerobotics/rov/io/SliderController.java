package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.config.SliderConfig;
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
    private final Observable<MotionPowerValue> motion;

    private final Observable<LightSpeedValue> lights;

    public SliderController(
        final Arduino arduino,
        final Scheduler scheduler,
        @Event final Observable<MotionPowerValue> motionPowerValues,
        final SliderConfig config
    ) {
        // Get an observable for each of the power sliders
        final Observable<Float> global = arduino.analogPin(config.globalPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> heave = arduino.analogPin(config.heavePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> sway = arduino.analogPin(config.swayPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> surge = arduino.analogPin(config.surgePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> pitch = Observable.just(0f).map(AnalogToPowerLevel::convert);
        final Observable<Float> yaw = arduino.analogPin(config.yawPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convert);
        final Observable<Float> roll = arduino.analogPin(config.rollPowerSliderAddress())
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

        lights = arduino.analogPin(config.lightPowerSliderAddress())
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

