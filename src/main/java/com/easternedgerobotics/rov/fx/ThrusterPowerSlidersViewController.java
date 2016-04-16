package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class ThrusterPowerSlidersViewController implements ViewController {
    /**
     * The event publisher instance.
     */
    private final EventPublisher eventPublisher;

    /**
     * The @{code ThrusterPowerSlidersView} this controller works for.
     */
    private final ThrusterPowerSlidersView view;

    /**
     * The subscriptions held by this view controller.
     */
    private final CompositeSubscription subscriptions;

    /**
     * Constructs a @{code ThrusterPowerSlidersViewController} with the specified event publisher and view.
     *
     * @param eventPublisher the event publisher instance to use
     * @param view the @{code ThrusterPowerSlidersView} this controller works for
     */
    @Inject
    public ThrusterPowerSlidersViewController(
        final EventPublisher eventPublisher,
        final ThrusterPowerSlidersView view
    ) {
        this.eventPublisher = eventPublisher;
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public final void onCreate() {
        updateLabel(view.globalSliderView);
        updateLabel(view.heaveSliderView);
        updateLabel(view.swaySliderView);
        updateLabel(view.surgeSliderView);
        updateLabel(view.yawSliderView);
        updateLabel(view.rollSliderView);

        final Observable<Float> global = values(view.globalSliderView);
        final Observable<Float> heave = values(view.heaveSliderView);
        final Observable<Float> sway = values(view.swaySliderView);
        final Observable<Float> surge = values(view.surgeSliderView);
        // We don't actually show pitch, but it's nice to have pitch in the model,
        // so we want to emit a value for it.
        final Observable<Float> pitch = values(new SliderView("Pitch", 0));
        final Observable<Float> yaw = values(view.yawSliderView);
        final Observable<Float> roll = values(view.rollSliderView);

        subscriptions.add(
            Observable.combineLatest(global, heave, sway, surge, pitch, yaw, roll, MotionPowerValue::create)
                .observeOn(Schedulers.io())
                .subscribe(eventPublisher::emit));
    }

    @Override
    public final void onDestroy() {
        subscriptions.unsubscribe();
    }

    private String labelText(final Number value) {
        return String.format(SliderView.LABEL_FORMAT, value.floatValue());
    }

    private void updateLabel(final SliderView sliderView) {
        subscriptions.add(JavaFxObservable.fromObservableValue(sliderView.slider.valueProperty())
            .map(this::labelText)
            .subscribe(sliderView.valueLabel::setText));
    }

    private Observable<Float> values(final SliderView sliderView) {
        return JavaFxObservable.fromObservableValue(sliderView.slider.valueProperty())
            .map((value) -> (float) (value.floatValue() / SliderView.MAX_VALUE));
    }
}
