package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.io.PilotPanel;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class ThrusterPowerSlidersViewController implements ViewController {
    /**
     * The event publisher instance.
     */
    private final EventPublisher eventPublisher;

    /**
     * The pilot panel interface.
     */
    private final PilotPanel pilotPanel;

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

        subscriptions.add(pilotPanel.gloablPowerSlider().observeOn(jfxScheduler)
            .subscribe(globalSliderView.slider::setValue));
        subscriptions.add(pilotPanel.heavePowerSlider().observeOn(jfxScheduler)
            .subscribe(heaveSliderView.slider::setValue));
        subscriptions.add(pilotPanel.swayPowerSlider().observeOn(jfxScheduler)
            .subscribe(swaySliderView.slider::setValue));
        subscriptions.add(pilotPanel.surgePowerSlider().observeOn(jfxScheduler)
            .subscribe(surgeSliderView.slider::setValue));
        subscriptions.add(pilotPanel.pitchPowerSlider().observeOn(jfxScheduler)
            .subscribe(pitchSliderView.slider::setValue));
        subscriptions.add(pilotPanel.yawPowerSlider().observeOn(jfxScheduler)
            .subscribe(yawSliderView.slider::setValue));
        subscriptions.add(pilotPanel.rollPowerSlider().observeOn(jfxScheduler)
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
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private Observable<Float> values(final SliderView sliderView) {
        return JavaFxObservable.fromObservableValue(sliderView.slider.valueProperty())
            .map((value) -> (float) (value.floatValue() / SliderView.MAX_VALUE));
    }
}
