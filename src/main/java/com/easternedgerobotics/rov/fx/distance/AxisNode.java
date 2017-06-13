package com.easternedgerobotics.rov.fx.distance;

import com.easternedgerobotics.rov.value.AxisValue;
import com.easternedgerobotics.rov.value.PointValue;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AxisNode implements ShapeNode {
    private static final int CIRCLE_SIZE = 7;

    private static final int STROKE_WIDTH = 5;

    private static final double XY_AXIS_OFFSET = 0.10;

    private static final double Z_AXIS_OFFSET = 0.10;

    private final Shape shape;

    private final Line xLine;

    private final Line yLine;

    private final Line zLine;

    private final List<ShapeNode> children = new ArrayList<>();

    private final TextNode xNode;

    private final TextNode yNode;

    private final TextNode zNode;

    private volatile double x;

    private volatile double y;

    public AxisNode() {
        shape = new Circle(CIRCLE_SIZE, Color.RED);
        xNode = new TextNode(new Circle(CIRCLE_SIZE, Color.BLUE));
        yNode = new TextNode(new Circle(CIRCLE_SIZE, Color.GREEN));
        zNode = new TextNode(new Circle(CIRCLE_SIZE, Color.ORANGE));
        xNode.setText("X Axis");
        yNode.setText("Y Axis");
        zNode.setText("Z Axis");
        children.add(xNode);
        children.add(yNode);
        children.add(zNode);
        xNode.nodeMoved(XY_AXIS_OFFSET, -XY_AXIS_OFFSET);
        yNode.nodeMoved(-XY_AXIS_OFFSET, -XY_AXIS_OFFSET);
        zNode.nodeMoved(0.00, -Z_AXIS_OFFSET);
        xLine = new Line();
        yLine = new Line();
        zLine = new Line();
        xLine.setStrokeWidth(STROKE_WIDTH);
        yLine.setStrokeWidth(STROKE_WIDTH);
        zLine.setStrokeWidth(STROKE_WIDTH);
        xLine.setStroke(Color.BLUE);
        yLine.setStroke(Color.GREEN);
        zLine.setStroke(Color.ORANGE);
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
    public void nodeMoved(final double dx, final double dy) {
        x += dx;
        y += dy;
        children.forEach(n -> n.nodeMoved(dx, dy));
    }

    @Override
    public Shape getHandleShape() {
        return shape;
    }

    @Override
    public List<Shape> getDrawnShapes() {
        return Arrays.asList(xLine, yLine, zLine, shape);
    }

    @Override
    public List<ShapeNode> getChildren() {
        return children;
    }

    @Override
    public void toFront() {
        xLine.toFront();
        yLine.toFront();
        zLine.toFront();
        shape.toFront();
        children.forEach(ShapeNode::toFront);
    }

    @Override
    public void draw(final double sceneWidth, final double sceneHeight) {
        if (shape != null) {
            final double x0 = x * sceneWidth;
            final double y0 = y * sceneHeight;
            shape.setTranslateX(x0);
            shape.setTranslateY(y0);
            xLine.setStartX(x0);
            yLine.setStartX(x0);
            zLine.setStartX(x0);
            xLine.setStartY(y0);
            yLine.setStartY(y0);
            zLine.setStartY(y0);
            xLine.setEndX(xNode.getX() * sceneWidth);
            yLine.setEndX(yNode.getX() * sceneWidth);
            zLine.setEndX(zNode.getX() * sceneWidth);
            xLine.setEndY(xNode.getY() * sceneHeight);
            yLine.setEndY(yNode.getY() * sceneHeight);
            zLine.setEndY(zNode.getY() * sceneHeight);
            xLine.setTranslateX((x0 + xNode.getX() * sceneWidth) / 2);
            yLine.setTranslateX((x0 + yNode.getX() * sceneWidth) / 2);
            zLine.setTranslateX((x0 + zNode.getX() * sceneWidth) / 2);
            xLine.setTranslateY((y0 + xNode.getY() * sceneHeight) / 2);
            yLine.setTranslateY((y0 + yNode.getY() * sceneHeight) / 2);
            zLine.setTranslateY((y0 + zNode.getY() * sceneHeight) / 2);
        }
    }

    /**
     * Convert this node into a value object representation.
     *
     * @return a new axis value.
     */
    public AxisValue getAxis() {
        return new AxisValue(new PointValue(x, y), xNode.getPoint(), yNode.getPoint(), zNode.getPoint());
    }
}
