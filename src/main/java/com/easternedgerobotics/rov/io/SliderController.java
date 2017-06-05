package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.config.SliderConfig;
import com.easternedgerobotics.rov.control.AnalogToPowerLevel;
import com.easternedgerobotics.rov.control.SourceController;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.arduino.Arduino;
import com.easternedgerobotics.rov.value.AftPowerValue;
import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.ForePowerValue;
import com.easternedgerobotics.rov.value.GlobalPowerValue;
import com.easternedgerobotics.rov.value.HeavePowerValue;
import com.easternedgerobotics.rov.value.LightASpeedValue;
import com.easternedgerobotics.rov.value.LightBSpeedValue;
import com.easternedgerobotics.rov.value.PitchPowerValue;
import com.easternedgerobotics.rov.value.SurgePowerValue;
import com.easternedgerobotics.rov.value.SwayPowerValue;
import com.easternedgerobotics.rov.value.YawPowerValue;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class SliderController {
    private final CompositeSubscription subscription = new CompositeSubscription();

    public SliderController(
        final Arduino arduino,
        final Scheduler scheduler,
        final EventPublisher eventPublisher,
        final SliderConfig config
    ) {
        final Observable<GlobalPowerValue> global = arduino.analogPin(config.globalPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(GlobalPowerValue::new);

        final Observable<HeavePowerValue> heave = arduino.analogPin(config.heavePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(HeavePowerValue::new);

        final Observable<SwayPowerValue> sway = arduino.analogPin(config.swayPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(SwayPowerValue::new);

        final Observable<SurgePowerValue> surge = arduino.analogPin(config.surgePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(SurgePowerValue::new);

        final Observable<PitchPowerValue> pitch = arduino.analogPin(config.pitchPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(PitchPowerValue::new);

        final Observable<YawPowerValue> yaw = arduino.analogPin(config.yawPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(YawPowerValue::new);

        final Observable<AftPowerValue> aft = arduino.analogPin(config.aftPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(AftPowerValue::new);

        final Observable<ForePowerValue> fore = arduino.analogPin(config.forePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(ForePowerValue::new);

        final Observable<LightASpeedValue> lightA = arduino.analogPin(config.lightAPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(LightASpeedValue::new);

        final Observable<LightBSpeedValue> lightB = arduino.analogPin(config.lightBPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(LightBSpeedValue::new);

        subscription.addAll(
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(GlobalPowerValue.class), v -> { }, scheduler,
                global, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(HeavePowerValue.class), v -> { }, scheduler,
                heave, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(SwayPowerValue.class), v -> { }, scheduler,
                sway, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(SurgePowerValue.class), v -> { }, scheduler,
                surge, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(PitchPowerValue.class), v -> { }, scheduler,
                pitch, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(YawPowerValue.class), v -> { }, scheduler,
                yaw, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(AftPowerValue.class), v -> { }, scheduler,
                aft, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(ForePowerValue.class), v -> { }, scheduler,
                fore, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(LightASpeedValue.class), v -> { }, scheduler,
                lightA, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(LightBSpeedValue.class), v -> { }, scheduler,
                lightB, eventPublisher::emit, Schedulers.io()));
    }

    public void stop() {
        subscription.clear();
    }
}
