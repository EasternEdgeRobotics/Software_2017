package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.control.TwoActionButton;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.MotionPowerProfile;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.stream.IntStream;
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
     * The pilot panel interface.
     */
    private final PilotPanel pilotPanel;

    /**
     * Contains saved motion power profiles.
     */
    private final MotionPowerProfile profile;

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
     * @param pilotPanel the pilot panel instance to use
     * @param profiles the motion power profile instance to use
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
        final PilotPanel pilotPanel,
        final MotionPowerProfile profiles,
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
        this.pilotPanel = pilotPanel;
        this.profile = profiles;
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

        // Safety first!
        globalSliderView.slider.setValue(0);

        view.row.getChildren().addAll(
            globalSliderView.getParent(),
            heaveSliderView.getParent(),
            swaySliderView.getParent(),
            surgeSliderView.getParent(),
            yawSliderView.getParent(),
            rollSliderView.getParent()
        );

        subscriptions.add(pilotPanel.gloablPowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(globalSliderView.slider::setValue));
        subscriptions.add(pilotPanel.heavePowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(heaveSliderView.slider::setValue));
        subscriptions.add(pilotPanel.swayPowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(swaySliderView.slider::setValue));
        subscriptions.add(pilotPanel.surgePowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(surgeSliderView.slider::setValue));
        subscriptions.add(pilotPanel.pitchPowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(pitchSliderView.slider::setValue));
        subscriptions.add(pilotPanel.yawPowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(yawSliderView.slider::setValue));
        subscriptions.add(pilotPanel.rollPowerSlider().observeOn(JAVA_FX_SCHEDULER)
            .subscribe(rollSliderView.slider::setValue));

        final Observable<Float> global = values(globalSliderView);
        final Observable<Float> heave = values(heaveSliderView);
        final Observable<Float> sway = values(swaySliderView);
        final Observable<Float> surge = values(surgeSliderView);
        final Observable<Float> pitch = values(pitchSliderView);
        final Observable<Float> yaw = values(yawSliderView);
        final Observable<Float> roll = values(rollSliderView);

        subscriptions.add(
            Observable.combineLatest(global, heave, sway, surge, pitch, yaw, roll, MotionPowerValue::new)
                .observeOn(Schedulers.io())
                .subscribe(eventPublisher::emit));

        IntStream.range(0, PilotPanel.LIGHT_BUTTON_COUNT).forEach(i -> {
            final TwoActionButton button = new TwoActionButton(pilotPanel.click(i), LONG_CLICK, JAVA_FX_SCHEDULER);
            subscriptions.add(button.shortClick().filter(x -> x).map(x -> profile.get(i)).subscribe(this::setPower));
            subscriptions.add(button.longClick().filter(x -> x).subscribe(x -> profile.set(i, getPower())));
        });
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private Observable<Float> values(final SliderView sliderView) {
        return JavaFxObservable.fromObservableValue(sliderView.slider.valueProperty())
            .map((value) -> (float) (value.floatValue() / SliderView.MAX_VALUE));
    }

    private void setPower(final MotionPowerValue value) {
        globalSliderView.slider.setValue(powerToSliderValue(value.getGlobal()));
        heaveSliderView.slider.setValue(powerToSliderValue(value.getHeave()));
        swaySliderView.slider.setValue(powerToSliderValue(value.getSway()));
        surgeSliderView.slider.setValue(powerToSliderValue(value.getSurge()));
        pitchSliderView.slider.setValue(powerToSliderValue(value.getPitch()));
        yawSliderView.slider.setValue(powerToSliderValue(value.getYaw()));
        rollSliderView.slider.setValue(powerToSliderValue(value.getRoll()));
    }

    private static int powerToSliderValue(final float power) {
        return (int) (power * SliderView.MAX_VALUE);
    }

    private MotionPowerValue getPower() {
        return new MotionPowerValue(
            sliderValueToPower(globalSliderView),
            sliderValueToPower(heaveSliderView),
            sliderValueToPower(swaySliderView),
            sliderValueToPower(surgeSliderView),
            sliderValueToPower(pitchSliderView),
            sliderValueToPower(yawSliderView),
            sliderValueToPower(rollSliderView)
        );
    }

    private static float sliderValueToPower(final SliderView sliderView) {
        return  (float) (sliderView.slider.valueProperty().floatValue() / SliderView.MAX_VALUE);
    }
}
