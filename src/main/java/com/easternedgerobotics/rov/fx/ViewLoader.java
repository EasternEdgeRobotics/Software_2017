package com.easternedgerobotics.rov.fx;

import com.google.inject.Injector;
import com.google.inject.Module;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.inject.Inject;

/**
 * A @{code ViewLoader} is responsible for loading a view, its corresponding controller, and wiring the two together.
 */
public class ViewLoader {
    private final Injector injector;

    /**
     * Constructs a @{code ViewLoader} with the specified @{link Injector}.
     * @param injector the injector to use when injecting views and controllers
     */
    @Inject
    public ViewLoader(final Injector injector) {
        this.injector = injector;
    }

    /**
     * Loads the view into the specified stage.
     * <p>
     * This method creates and modifies @{link Stage} and @{link Scene} objects. As such, it
     * must be called on the JavaFX Application Thread.
     *
     * @param viewClass the view class
     * @param stage the stage into which the view should be loaded
     * @param <T> the type of the view
     */
    public final <T extends View> void loadIntoStage(final Class<T> viewClass, final Stage stage) {
        final Pair<View, ViewController> view = makeCarelessly(viewClass);
        final ViewController viewController = view.getValue();
        final Scene scene = new Scene(view.getKey().getParent());
        stage.setScene(scene);
        stage.setOnShown(x -> viewController.onCreate());
        stage.setOnHidden(x -> viewController.onDestroy());
    }

    /**
     * Loads the view into the a new stage.
     * <p>
     * This method creates and modifies @{link Stage} and @{link Scene} objects. As such, it
     * must be called on the JavaFX Application Thread.
     *
     * @param viewClass the view class
     * @param <T> the type of the view
     * @return the stage the view was loaded into
     */
    public final <T extends View> Stage load(final Class<T> viewClass) {
        final Stage stage = new Stage();
        loadIntoStage(viewClass, stage);
        return stage;
    }

    /**
     * Creates the view and its controller.
     * @param viewClass the view class
     * @param <T> the view type
     * @return the instantiated view and its controller
     * @throws ClassNotFoundException if the view controller for the given view cannot be found
     */
    private <T extends View> Pair<View, ViewController> make(final Class<T> viewClass) throws ClassNotFoundException {
        final Module viewModule = binder -> binder.bind(viewClass).asEagerSingleton();
        final Injector viewInjector = injector.createChildInjector(viewModule);
        final ViewController viewController = (ViewController) viewInjector.getInstance(
            Class.forName(viewClass.getName() + "Controller"));
        return new Pair<>(viewInjector.getInstance(viewClass), viewController);
    }

    private <T extends View> Pair<View, ViewController> makeCarelessly(final Class<T> viewClass) {
        try {
            return make(viewClass);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
