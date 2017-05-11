package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.control.SourceController;
import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.AftPowerValue;
import com.easternedgerobotics.rov.value.ForePowerValue;
import com.easternedgerobotics.rov.value.GlobalPowerValue;
import com.easternedgerobotics.rov.value.HeavePowerValue;
import com.easternedgerobotics.rov.value.PitchPowerValue;
import com.easternedgerobotics.rov.value.RollPowerValue;
import com.easternedgerobotics.rov.value.SpeedValue;
import com.easternedgerobotics.rov.value.SurgePowerValue;
import com.easternedgerobotics.rov.value.SwayPowerValue;
import com.easternedgerobotics.rov.value.YawPowerValue;

import javafx.scene.control.Slider;
import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.function.Consumer;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class ThrusterPowerSlidersViewController implements ViewController {
    /**
     * Long click duration for profile buttons.
     */
    private static final int LONG_CLICK = 2000;

    /**
     * The event publisher instance.
     */
    private final EventPublisher eventPublisher;

    /**
     * The event for global power.
     */
    private final Observable<GlobalPowerValue> globalPowerValues;

    /**
     * The event for heave power.
     */
    private final Observable<HeavePowerValue> heavePowerValues;

    /**
     * The event for sway power.
     */
    private final Observable<SwayPowerValue> swayPowerValues;

    /**
     * The event for surge power.
     */
    private final Observable<SurgePowerValue> surgePowerValues;

    /**
     * The event for pitch power.
     */
    private final Observable<PitchPowerValue> pitchPowerValues;

    /**
     * The event for yaw power.
     */
    private final Observable<YawPowerValue> yawPowerValues;

    /**
     * The event for roll power.
     */
    private final Observable<RollPowerValue> rollPowerValues;

    /**
     * The event for aft power.
     */
    private final Observable<AftPowerValue> aftPowerValues;

    /**
     * The event for fore power.
     */
    private final Observable<ForePowerValue> forePowerValues;

    /**
     * The @{code ThrusterPowerSlidersView} this controller works for.
     */
    private final ThrusterPowerSlidersView view;

    /**
     * The slider for global power.
     */
    private final SliderView globalSliderView;

    /**
     * The slider for power in the heave direction.
     */
    private final SliderView heaveSliderView;

    /**
     * The slider for power in the sway direction.
     */
    private final SliderView swaySliderView;

    /**
     * The slider for power in the surge direction.
     */
    private final SliderView surgeSliderView;

    /**
     * The slider for power in the pitch direction.
     */
    private final SliderView pitchSliderView;

    /**
     * The slider for power in the yaw direction.
     */
    private final SliderView yawSliderView;

    /**
     * The slider for power in the roll direction.
     */
    private final SliderView rollSliderView;

    /**
     * The slider for power in the yaw direction.
     */
    private final SliderView aftSliderView;

    /**
     * The slider for power in the roll direction.
     */
    private final SliderView foreSliderView;



    /**
     * The subscriptions held by this view controller.
     */
    private final CompositeSubscription subscriptions;

    /**
     * Constructs a {@code ThrusterPowerSlidersViewController} with the specified event publisher and view.
     *
     * @param eventPublisher the event publisher instance to use
     * @param globalPowerValues the global power value observable
     * @param heavePowerValues the heave power value observable
     * @param swayPowerValues the sway power value observable
     * @param surgePowerValues the surge power value observable
     * @param pitchPowerValues the pitch power value observable
     * @param yawPowerValues the yaw power value observable
     * @param rollPowerValues the roll power value observable
     * @param aftPowerValues the aft power value observable
     * @param forePowerValues the fore power value observable
     * @param view the {@code ThrusterPowerSlidersView} this controller works for
     * @param globalSliderView the global power slider view
     * @param heaveSliderView the heave power slider view
     * @param swaySliderView the sway power slider view
     * @param surgeSliderView the surge power slider view
     * @param pitchSliderView the pitch power slider view
     * @param yawSliderView the yaw power slider view
     * @param rollSliderView the roll power slider view
     */
    @Inject
    public ThrusterPowerSlidersViewController(
        final EventPublisher eventPublisher,
        @Event final Observable<GlobalPowerValue> globalPowerValues,
        @Event final Observable<HeavePowerValue> heavePowerValues,
        @Event final Observable<SwayPowerValue> swayPowerValues,
        @Event final Observable<SurgePowerValue> surgePowerValues,
        @Event final Observable<PitchPowerValue> pitchPowerValues,
        @Event final Observable<YawPowerValue> yawPowerValues,
        @Event final Observable<RollPowerValue> rollPowerValues,
        @Event final Observable<AftPowerValue> aftPowerValues,
        @Event final Observable<ForePowerValue> forePowerValues,
        final ThrusterPowerSlidersView view,
        final SliderView globalSliderView,
        final SliderView heaveSliderView,
        final SliderView swaySliderView,
        final SliderView surgeSliderView,
        final SliderView pitchSliderView,
        final SliderView yawSliderView,
        final SliderView rollSliderView,
        final SliderView aftSliderView,
        final SliderView foreSliderView
    ) {
        this.eventPublisher = eventPublisher;
        this.globalPowerValues = globalPowerValues;
        this.heavePowerValues = heavePowerValues;
        this.swayPowerValues = swayPowerValues;
        this.surgePowerValues = surgePowerValues;
        this.pitchPowerValues = pitchPowerValues;
        this.yawPowerValues = yawPowerValues;
        this.rollPowerValues = rollPowerValues;
        this.aftPowerValues = aftPowerValues;
        this.forePowerValues = forePowerValues;
        this.view = view;
        this.globalSliderView = globalSliderView;
        this.heaveSliderView = heaveSliderView;
        this.swaySliderView = swaySliderView;
        this.surgeSliderView = surgeSliderView;
        this.pitchSliderView = pitchSliderView;
        this.yawSliderView = yawSliderView;
        this.rollSliderView = rollSliderView;
        this.aftSliderView = aftSliderView;
        this.foreSliderView = foreSliderView;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        globalSliderView.displayNameLabel.setText("Global");
        heaveSliderView.displayNameLabel.setText("Heave");
        swaySliderView.displayNameLabel.setText("Sway");
        surgeSliderView.displayNameLabel.setText("Surge");
        pitchSliderView.displayNameLabel.setText("Pitch");
        yawSliderView.displayNameLabel.setText("Yaw");
        aftSliderView.displayNameLabel.setText("aft");
        foreSliderView.displayNameLabel.setText("fore");

        view.row.getChildren().addAll(
            globalSliderView.getParent(),
            heaveSliderView.getParent(),
            swaySliderView.getParent(),
            surgeSliderView.getParent(),
            pitchSliderView.getParent(),
            yawSliderView.getParent(),
            aftSliderView.getParent(),
            foreSliderView.getParent()
        );

        subscriptions.addAll(
            SourceController.manageMultiViewModel(
                globalPowerValues, sliderSetter(globalSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(globalSliderView).map(GlobalPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                heavePowerValues, sliderSetter(heaveSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(heaveSliderView).map(HeavePowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                swayPowerValues, sliderSetter(swaySliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(swaySliderView).map(SwayPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                surgePowerValues, sliderSetter(surgeSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(surgeSliderView).map(SurgePowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                pitchPowerValues, sliderSetter(pitchSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(pitchSliderView).map(PitchPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                yawPowerValues, sliderSetter(yawSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(yawSliderView).map(YawPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                rollPowerValues, sliderSetter(rollSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(rollSliderView).map(RollPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                aftPowerValues, sliderSetter(aftSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(aftSliderView).map(AftPowerValue::new), eventPublisher::emit, Schedulers.io()),
            SourceController.manageMultiViewModel(
                forePowerValues, sliderSetter(foreSliderView.slider), JAVA_FX_SCHEDULER,
                sliderGetter(foreSliderView).map(ForePowerValue::new), eventPublisher::emit, Schedulers.io()));

        setSliderPower(globalSliderView.slider, 0);
        setSliderPower(heaveSliderView.slider, 1);
        setSliderPower(swaySliderView.slider, 1);
        setSliderPower(surgeSliderView.slider, 1);
        setSliderPower(pitchSliderView.slider, 1);
        setSliderPower(yawSliderView.slider, 1);
        setSliderPower(rollSliderView.slider, 1);
        setSliderPower(aftSliderView.slider, 1);
        setSliderPower(foreSliderView.slider, 1);
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private Observable<Float> sliderGetter(final SliderView sliderView) {
        return JavaFxObservable.valuesOf(sliderView.slider.valueProperty())
            .startWith(sliderView.slider.getValue())
            .map((value) -> (float) (value.floatValue() / SliderView.MAX_VALUE));
    }

    private static Consumer<SpeedValue> sliderSetter(final Slider slider) {
        return power -> setSliderPower(slider, power.getSpeed());
    }

    private static void setSliderPower(final Slider slider, final float power) {
        slider.setValue((int) (power * SliderView.MAX_VALUE));
    }
}
