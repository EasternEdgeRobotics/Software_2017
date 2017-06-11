package com.easternedgerobotics.rov.fx.distance;

import com.easternedgerobotics.rov.value.PointValue;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextNode implements ShapeNode {
    private static final int FONT_SIZE = 20;

    private static final int FONT_OFFSET = 20;

    private final Shape shape;

    private final Text text;

    private final List<ShapeNode> children = new ArrayList<>();

    private volatile double x;

    private volatile double y;

    public TextNode(final Shape shape) {
        this.shape = shape;
        text = new Text();
        text.setFont(Font.font("Verdana", FontWeight.BOLD, FONT_SIZE));
        text.setStroke(Color.BLACK);
        text.setStrokeWidth(1);
        text.setFill(Color.WHITE);
    }

    @Override
    public void nodeMoved(final double dx, final double dy) {
        x += dx;
        y += dy;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public Shape getHandleShape() {
        return shape;
    }

    @Override
    public List<Shape> getDrawnShapes() {
        return Arrays.asList(shape, text);
    }

    public List<ShapeNode> getChildren() {
        return children;
    }

    @Override
    public void toFront() {
        shape.toFront();
        text.toFront();
    }

    @Override
    public void draw(final double sceneWidth, final double sceneHeight) {
        if (shape != null) {
            shape.setTranslateX(x * sceneWidth);
            shape.setTranslateY(y * sceneHeight);
            text.setTranslateX(x * sceneWidth);
            text.setTranslateY((y * sceneHeight) - FONT_OFFSET);
        }
    }

    /**
     * Set the text displayed next to this node.
     *
     * @param label the new text.
     */
    public void setText(final String label) {
        text.setText(label);
    }

    /**
     * Convert this node into a value object representation.
     *
     * @return a new point value.
     */
    public PointValue getPoint() {
        return new PointValue(x, y);
    }
}
