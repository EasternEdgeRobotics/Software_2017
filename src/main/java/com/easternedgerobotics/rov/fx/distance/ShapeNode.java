package com.easternedgerobotics.rov.fx.distance;

import javafx.scene.shape.Shape;

import java.util.List;

public interface ShapeNode {
    /**
     * Get the horizontal position in percentage from the center of the image to the edge of the scene.
     *
     * @return the x value
     */
    double getX();

    /**
     * Get the vertical position in percentage from the center of the image to the edge of the scene.
     *
     * @return the y value
     */
    double getY();

    /**
     * Update the location of the node by the given offset.
     *
     * @param dx percentage change in horizontal
     * @param dy percentage change in vertical
     */
    void nodeMoved(final double dx, final double dy);

    /**
     * Get the shape which is used to interact with the node.
     *
     * @return the handle
     */
    Shape getHandleShape();

    /**
     * Get the shapes used to represent this node on screen.
     *
     * @return the drawn shapes of the node.
     */
    List<Shape> getDrawnShapes();

    /**
     * The nodes which should be updated when this node is updated.
     *
     * @return the children shapes.
     */
    List<ShapeNode> getChildren();

    /**
     * Bring the shapes in the node to the front of its container.
     */
    void toFront();

    /**
     * Update the x and y position of the contained shape to be displayed in the given screen size.
     *
     * @param sceneWidth the target screen width.
     * @param sceneHeight the target screen height.
     */
    void draw(final double sceneWidth, final double sceneHeight);
}
