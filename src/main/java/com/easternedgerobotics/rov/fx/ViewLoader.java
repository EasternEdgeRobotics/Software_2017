package com.easternedgerobotics.rov.fx;

import com.easternedgerobotics.rov.event.Event;
import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.value.MutableValueCompanion;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import javax.inject.Inject;

public class ViewLoader {
    private final Map<Class<?>, Object> dependencies;

    public ViewLoader(final Map<Class<?>, Object> dependencies) {
        this.dependencies = new WeakHashMap<>(dependencies);
    }

    /**
     * Loads the view into the specified stage.
     * <p>
     * This method creates and modifies {@link Stage} and {@link Scene} objects. As such, it
     * must be called on the JavaFX Application Thread.
     *
     * @param viewClass the view class
     * @param stage the stage into which the view should be loaded
     * @param <T> the type of the view
     */
    public final <T extends View> void loadIntoStage(final Class<T> viewClass, final Stage stage) {
        final Pair<View, ViewController> view = makeView(viewClass, dependencies);
        final ViewController viewController = view.getValue();
        final Scene scene = new Scene(view.getKey().getParent());
        stage.setScene(scene);
        stage.setOnShown(event -> {
            viewController.onCreate();
            stage.sizeToScene();
        });
        stage.setOnHidden(event -> {
            viewController.onDestroy();
        });
    }

    /**
     * Loads the view into the a new stage.
     * <p>
     * This method creates and modifies {@link Stage} and {@link Scene} objects. As such, it
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
     * @param dependencies the dependencies to use when constructing the view and its controller
     * @param <T> the view type
     * @return the instantiated view and its controller
     */
    @SuppressWarnings("unchecked")
    private <T extends View> Pair<View, ViewController> makeView(
        final Class<T> viewClass,
        final Map<Class<?>, Object> dependencies
    ) {
        final Map<Class<?>, Object> subtreeDependencies = new WeakHashMap<>(dependencies);
        final Class<?> viewControllerClass = carelessly(() -> Class.forName(viewClass.getName() + "Controller"));
        final View view = (View) subtreeDependencies.computeIfAbsent(viewClass, k -> make(viewClass, dependencies));
        final ViewController viewController = (ViewController) subtreeDependencies.computeIfAbsent(
            viewControllerClass, k -> make(k, subtreeDependencies));
        return new Pair<>(view, viewController);
    }

    /**
     * Creates an object of the given class.
     * @param clazz the object class
     * @param dependencies the dependencies to use when constructing the object
     * @param <T> the object type
     * @return the instantiated object
     */
    @SuppressWarnings("unchecked")
    private <T> T make(final Class<T> clazz, final Map<Class<?>, Object> dependencies) {
        final CompositeViewController viewController = new CompositeViewController();
        final Constructor<?>[] constructors = clazz.getConstructors();
        final Optional<Constructor<?>> constructor = Arrays.stream(constructors).filter(this::isInjectable).findFirst();

        if (!constructor.isPresent()) {
            throw new RuntimeException(
                String.format("Could not find a public @javax.inject.Inject annotated constructor for %s", clazz));
        }

        final Constructor<?> ctor = constructor.get();
        final Stream<Object> args = Arrays.stream(ctor.getParameters()).map(parameter -> {
            final Class<?> type = parameter.getType();
            if (dependencies.containsKey(type)) {
                return dependencies.get(type);
            }
            if (View.class.isAssignableFrom(type)) {
                final Pair<View, ViewController> pair = makeView((Class<? extends View>) type, dependencies);
                viewController.add(pair.getValue());

                return pair.getKey();
            }
            if (parameter.isAnnotationPresent(Event.class)) {
                final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                final Class<?> inner = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (MutableValueCompanion.class.isAssignableFrom(inner)) {
                    return ((EventPublisher) dependencies.get(EventPublisher.class)).valuesOfType(
                        (Class<? extends MutableValueCompanion>) inner);
                }
            }
            throw new RuntimeException(String.format("%s could not be resolved while constructing %s", type, clazz));
        });

        return carelessly(() -> {
            final Object instance = ctor.newInstance(args.toArray());
            if (instance instanceof View) {
                return (T) instance;
            } else {
                viewController.add((ViewController) instance);
                return (T) viewController.collapse();
            }
        });
    }

    /**
     * Returns whether or not the given constructor is marked for injection.
     * <p>
     * This checks for the presence of the {@link Inject} annotation.
     * @param constructor the constructor to check
     * @return whether or not the given constructor is marked for injection
     */
    private boolean isInjectable(final Constructor<?> constructor) {
        return constructor.isAnnotationPresent(Inject.class);
    }

    /**
     * Invoke the given function carelessly.
     * <p>
     * All exceptions are upgraded to runtime exceptions.
     * @param fn the function to invoke
     * @param <T> the return type of the function
     * @return the result of the given function
     */
    private <T> T carelessly(final Callable<T> fn) {
        try {
            return fn.call();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
