package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.control.SupressObservable;
import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import javafx.scene.control.Slider;
import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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

    private final Observable<MotionPowerValue> motionPowerValues;

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
     * The subscriptions held by this view controller.
     */
    private final CompositeSubscription subscriptions;

    /**
     * Constructs a {@code ThrusterPowerSlidersViewController} with the specified event publisher and view.
     *
     * @param eventPublisher the event publisher instance to use
     * @param motionPowerValues
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
        @Event final Observable<MotionPowerValue> motionPowerValues,
        final ThrusterPowerSlidersView view,
        final SliderView globalSliderView,
        final SliderView heaveSliderView,
        final SliderView swaySliderView,
        final SliderView surgeSliderView,
        final SliderView pitchSliderView,
        final SliderView yawSliderView,
        final SliderView rollSliderView
    ) {
        this.eventPublisher = eventPublisher;
        this.motionPowerValues = motionPowerValues;
        this.view = view;
        this.globalSliderView = globalSliderView;
        this.heaveSliderView = heaveSliderView;
        this.swaySliderView = swaySliderView;
        this.surgeSliderView = surgeSliderView;
        this.pitchSliderView = pitchSliderView;
        this.yawSliderView = yawSliderView;
        this.rollSliderView = rollSliderView;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        globalSliderView.displayNameLabel.setText("Global");
        heaveSliderView.displayNameLabel.setText("Heave");
        swaySliderView.displayNameLabel.setText("Sway");
        surgeSliderView.displayNameLabel.setText("Surge");
        yawSliderView.displayNameLabel.setText("Yaw");
        rollSliderView.displayNameLabel.setText("Roll");

        view.row.getChildren().addAll(
            globalSliderView.getParent(),
            heaveSliderView.getParent(),
            swaySliderView.getParent(),
            surgeSliderView.getParent(),
            yawSliderView.getParent(),
            rollSliderView.getParent()
        );

        final SupressObservable<MotionPowerValue> external = new SupressObservable<>(
            motionPowerValues, Schedulers.io(), 2000);

        final SupressObservable<MotionPowerValue> internal = new SupressObservable<>(
            Observable.combineLatest(
                values(globalSliderView),
                values(heaveSliderView),
                values(swaySliderView),
                values(surgeSliderView),
                values(pitchSliderView),
                values(yawSliderView),
                values(rollSliderView),
                MotionPowerValue::new),
            Schedulers.io(), 2000);

        subscriptions.add(external.get()
            .startWith(new MotionPowerValue(0, 1, 1, 1, 1, 1, 1))
            .observeOn(JAVA_FX_SCHEDULER)
            .subscribe(value -> {
                internal.supress();
                setSliderPower(globalSliderView.slider, value.getGlobal());
                setSliderPower(heaveSliderView.slider, value.getHeave());
                setSliderPower(swaySliderView.slider, value.getSway());
                setSliderPower(surgeSliderView.slider, value.getSurge());
                setSliderPower(pitchSliderView.slider, value.getPitch());
                setSliderPower(yawSliderView.slider, value.getYaw());
                setSliderPower(rollSliderView.slider, value.getRoll());
            }));

        subscriptions.add(internal.get()
            .observeOn(JAVA_FX_SCHEDULER)
            .subscribe(value -> {
                external.supress();
                eventPublisher.emit(value);
            }));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private Observable<Float> values(final SliderView sliderView) {
        return JavaFxObservable.valuesOf(sliderView.slider.valueProperty())
            .startWith(sliderView.slider.getValue())
            .map((value) -> (float) (value.floatValue() / SliderView.MAX_VALUE));
    }

    private static void setSliderPower(final Slider slider, final float power) {
        slider.setValue((int) (power * SliderView.MAX_VALUE));
    }
}
