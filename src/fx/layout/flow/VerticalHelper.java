package fx.layout.flow;

import fx.react.value.Val;
import fx.react.value.Var;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Implementation of {@link OrientationHelper} where {@code breadth} represents width of the node/viewport and
 * {@code length} represents the height of the node/viewport. "layoutX" is {@link javafx.scene.Node#layoutX} and
 * "layoutY" is {@link javafx.scene.Node#layoutY}. "viewport offset" values are based on height. The viewport's "top"
 * and "bottom" edges are either it's top/bottom edges (See {@link fx.layout.flow.Gravity}).
 */
class VerticalHelper implements OrientationHelper {

  @Override
  public Orientation getContentBias() {
    return Orientation.HORIZONTAL;
  }

  @Override
  public double getX(double x, double y) {
    return x;
  }

  @Override
  public double getY(double x, double y) {
    return y;
  }

  @Override
  public double minBreadth(Node node) {
    return node.minWidth(-1);
  }

  @Override
  public double prefBreadth(Node node) {
    return node.prefWidth(-1);
  }

  @Override
  public double prefLength(Node node, double breadth) {
    return node.prefHeight(breadth);
  }

  @Override
  public double breadth(Bounds bounds) {
    return bounds.getWidth();
  }

  @Override
  public double length(Bounds bounds) {
    return bounds.getHeight();
  }

  @Override
  public double minX(Bounds bounds) {
    return bounds.getMinX();
  }

  @Override
  public double minY(Bounds bounds) {
    return bounds.getMinY();
  }

  @Override
  public double layoutX(Node node) {
    return node.getLayoutX();
  }

  @Override
  public double layoutY(Node node) {
    return node.getLayoutY();
  }

  @Override
  public DoubleProperty layoutYProperty(Node node) {
    return node.layoutYProperty();
  }

  @Override
  public void resizeRelocate(Node node, double b0, double l0, double breadth, double length) {
    node.resizeRelocate(b0, l0, breadth, length);
  }

  @Override
  public void resize(Node node, double breadth, double length) {
    node.resize(breadth, length);
  }

  @Override
  public void relocate(Node node, double b0, double l0) {
    node.relocate(b0, l0);
  }

  @Override
  public Val<Double> widthEstimateProperty(Viewport<?, ?> content) {
    return content.totalBreadthEstimateProperty();
  }

  @Override
  public Val<Double> heightEstimateProperty(Viewport<?, ?> content) {
    return content.totalLengthEstimateProperty();
  }

  @Override
  public Var<Double> estimatedScrollXProperty(Viewport<?, ?> content) {
    return content.breadthOffsetProperty();
  }

  @Override
  public Var<Double> estimatedScrollYProperty(Viewport<?, ?> content) {
    return content.lengthOffsetEstimateProperty();
  }

  @Override
  public void scrollHorizontallyBy(Viewport<?, ?> content, double dx) {
    content.scrollBreadth(dx);
  }

  @Override
  public void scrollVerticallyBy(Viewport<?, ?> content, double dy) {
    content.scrollLength(dy);
  }

  @Override
  public void scrollHorizontallyToPixel(Viewport<?, ?> content, double pixel) {
    content.setBreadthOffset(pixel);
  }

  @Override
  public void scrollVerticallyToPixel(Viewport<?, ?> content, double pixel) { // length
    content.setLengthOffset(pixel);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> hitBeforeCells(double bOff, double lOff) {
    return ViewportHit.hitBeforeCells(bOff, lOff);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> hitAfterCells(double bOff, double lOff) {
    return ViewportHit.hitAfterCells(bOff, lOff);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> cellHit(int itemIndex, C cell, double bOff, double lOff) {
    return ViewportHit.cellHit(itemIndex, cell, bOff, lOff);
  }

}