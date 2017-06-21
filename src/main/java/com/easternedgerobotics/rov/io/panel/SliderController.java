package com.easternedgerobotics.rov.io.panel;

import com.easternedgerobotics.rov.config.SliderConfig;
import com.easternedgerobotics.rov.control.AnalogToPowerLevel;
import com.easternedgerobotics.rov.control.SourceController;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.devices.IOBoard;
import com.easternedgerobotics.rov.value.AftPowerValue;
import com.easternedgerobotics.rov.value.AnalogPinValue;
import com.easternedgerobotics.rov.value.ForePowerValue;
import com.easternedgerobotics.rov.value.GlobalPowerValue;
import com.easternedgerobotics.rov.value.HeavePowerValue;
import com.easternedgerobotics.rov.value.LightAValue;
import com.easternedgerobotics.rov.value.LightBValue;
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
        final IOBoard io,
        final Scheduler scheduler,
        final EventPublisher eventPublisher,
        final SliderConfig config
    ) {
        final Observable<GlobalPowerValue> global = io.analogPin(config.globalPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(GlobalPowerValue::new);

        final Observable<HeavePowerValue> heave = io.analogPin(config.heavePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(HeavePowerValue::new);

        final Observable<SwayPowerValue> sway = io.analogPin(config.swayPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(SwayPowerValue::new);

        final Observable<SurgePowerValue> surge = io.analogPin(config.surgePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(SurgePowerValue::new);

        final Observable<PitchPowerValue> pitch = io.analogPin(config.pitchPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(PitchPowerValue::new);

        final Observable<YawPowerValue> yaw = io.analogPin(config.yawPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(YawPowerValue::new);

        final Observable<AftPowerValue> aft = io.analogPin(config.aftPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(AftPowerValue::new);

        final Observable<ForePowerValue> fore = io.analogPin(config.forePowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertNeg).map(ForePowerValue::new);

        final Observable<LightAValue> lightA = io.analogPin(config.lightAPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertBool).map(LightAValue::new);

        final Observable<LightBValue> lightB = io.analogPin(config.lightBPowerSliderAddress())
            .map(AnalogPinValue::getValue).map(AnalogToPowerLevel::convertBool).map(LightBValue::new);

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
                eventPublisher.valuesOfType(LightAValue.class), v -> { }, scheduler,
                lightA, eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                eventPublisher.valuesOfType(LightBValue.class), v -> { }, scheduler,
                lightB, eventPublisher::emit, Schedulers.io()));
    }

    public void stop() {
        subscription.clear();
    }
}
