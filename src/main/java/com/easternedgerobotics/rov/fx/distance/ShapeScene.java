package com.easternedgerobotics.rov.fx.distance;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import rx.observables.JavaFxObservable;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.List;

public final class ShapeScene {
    /**
     * A list of nodes within the scene which have no parent.
     */
    private final List<ShapeNode> children = new ArrayList<>();

    /**
     * The stack used to display these nodes.
     */
    private final StackPane stack;

    /**
     * The bounds used to locate node relative positions.
     */
    private final Node bounds;

    /**
     * Holds all subscriptions made by this scene.
     */
    private final CompositeSubscription subscriptions;

    /**
     * Construct a controller for the elements in a shape node scene.
     * Handles movement of the scene nodes and redraws the scene when required.
     *
     * @param stack the container of the node shapes.
     * @param bounds a node to use as the bounds of the scene.
     * @param subscriptions the holder of subscriptions created by this scene.
     */
    public ShapeScene(final StackPane stack, final Node bounds, final CompositeSubscription subscriptions) {
        this.stack = stack;
        this.bounds = bounds;
        this.subscriptions = subscriptions;
    }

    /**
     * Add a new node to the scene and manage movement and drawing.
     *
     * @param node the node to be managed.
     * @param containerX the starting x position of the node in the container (stack).
     * @param containerY the starting y position of the node in the container (stack).
     */
    public void add(final ShapeNode node, final double containerX, final double containerY) {
        children.add(node);
        node.nodeMoved(
            (containerX / bounds.getBoundsInParent().getWidth()) - (1.0 / 2.0),
            (containerY / bounds.getBoundsInParent().getHeight()) - (1.0 / 2.0));
        init(node);
        draw(node);
    }

    public void remove(final ShapeNode node) {
        children.remove(node);
        stack.getChildren().removeAll(node.getDrawnShapes());
        draw();
    }

    /**
     * Set up movement subscriptions for the node and add drawables to the scene.
     *
     * @param node the node to be initialized.
     */
    private void init(final ShapeNode node) {
        subscriptions.add(JavaFxObservable
            .eventsOf(node.getHandleShape(), EventType.ROOT)
            .filter(e -> e.getEventType().equals(MouseEvent.MOUSE_DRAGGED))
            .cast(MouseEvent.class)
            .filter(e -> e.getButton().equals(MouseButton.PRIMARY))
            .subscribe(e -> {
                node.toFront();
                node.nodeMoved(
                    e.getX() / bounds.getBoundsInParent().getWidth(),
                    e.getY() / bounds.getBoundsInParent().getHeight());
                draw();
            })
        );
        stack.getChildren().addAll(node.getDrawnShapes());
        node.getChildren().forEach(this::init);
    }

    /**
     * Redraw the nodes within in the scene.
     */
    public void draw() {
        children.forEach(this::draw);
    }

    /**
     * Redraw a node and all of its children within a scene.
     *
     * @param node the node to be redrawn.
     */
    private void draw(final ShapeNode node) {
        node.draw(bounds.getBoundsInParent().getWidth(), bounds.getBoundsInParent().getHeight());
        node.getChildren().forEach(this::draw);
    }
}
