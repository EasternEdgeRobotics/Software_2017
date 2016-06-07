package com.easternedgerobotics.rov.fx;

import rx.Scheduler;
import rx.schedulers.JavaFxScheduler;

/**
 * A view controller encapsulates the behaviour and logic required to render a view.
 * <p>
 * Each view controller sees lifecycle hooks when its view is the direct child of a scene inside a stage that is
 * being shown or hidden and it is responsible for passing along the lifecycle hooks to any views it creates (or
 * not).
 */
interface ViewController {
    Scheduler jfxScheduler = JavaFxScheduler.getInstance();

    /**
     * Called after the view for this controller is displayed.
     */
    default void onCreate() {
        // ???
    }

    /**
     * Called after the view for this controller has been destroyed.
     */
    default void onDestroy() {
        // ???
    }
}
