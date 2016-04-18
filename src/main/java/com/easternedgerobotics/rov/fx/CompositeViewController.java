package com.easternedgerobotics.rov.fx;

import java.util.ArrayDeque;
import java.util.Deque;

class CompositeViewController implements ViewController {
    private final Deque<ViewController> viewControllers;

    /**
     * Constructs an empty {@code CompositeViewController}.
     */
    CompositeViewController() {
        this.viewControllers = new ArrayDeque<>();
    }

    /**
     * Adds a new {@link ViewController} to this {@code CompositeViewController}.
     * @param viewController the view controller to add
     */
    void add(final ViewController viewController) {
        viewControllers.add(viewController);
    }

    /**
     * Returns the same view controller of the single entry in it.
     * @return the same view controller or the single entry in it
     */
    ViewController collapse() {
        if (viewControllers.size() == 1) {
            return viewControllers.pop();
        } else {
            return this;
        }
    }

    /**
     * Called after the view for this controller is displayed.
     * <p>
     * This will delegate to the controllers that compose it.
     */
    @Override
    public void onCreate() {
        viewControllers.forEach(ViewController::onCreate);
    }

    /**
     * Called after the view for this controller has been destroyed.
     * <p>
     * This will delegate to the controllers that compose it.
     */
    @Override
    public void onDestroy() {
        viewControllers.forEach(ViewController::onDestroy);
    }
}
