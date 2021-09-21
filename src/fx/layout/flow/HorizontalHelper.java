package fx.layout.flow;

import fx.react.value.Val;
import fx.react.value.Var;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Implementation of {@link OrientationHelper} where {@code length} represents width of the node/viewport and
 * {@code breadth} represents the height of the node/viewport. "layoutY" is {@link javafx.scene.Node#layoutX} and
 * "layoutX" is {@link javafx.scene.Node#layoutY}. "viewport offset" values are based on width. The viewport's "top"
 * and "bottom" edges are either it's left/right edges (See {@link fx.layout.flow.Gravity}).
 */
class HorizontalHelper implements OrientationHelper {

  @Override
  public Orientation getContentBias() {
    return Orientation.VERTICAL;
  }

  @Override
  public double getX(double x, double y) {
    return y;
  }

  @Override
  public double getY(double x, double y) {
    return x;
  }

  @Override
  public double minBreadth(Node node) {
    return node.minHeight(-1);
  }

  @Override
  public double prefBreadth(Node node) {
    return node.prefHeight(-1);
  }

  @Override
  public double prefLength(Node node, double breadth) {
    return node.prefWidth(breadth);
  }

  @Override
  public double breadth(Bounds bounds) {
    return bounds.getHeight();
  }

  @Override
  public double length(Bounds bounds) {
    return bounds.getWidth();
  }

  @Override
  public double minX(Bounds bounds) {
    return bounds.getMinY();
  }

  @Override
  public double minY(Bounds bounds) {
    return bounds.getMinX();
  }

  @Override
  public double layoutX(Node node) {
    return node.getLayoutY();
  }

  @Override
  public double layoutY(Node node) {
    return node.getLayoutX();
  }

  @Override
  public DoubleProperty layoutYProperty(Node node) {
    return node.layoutXProperty();
  }

  @Override
  public void resizeRelocate(Node node, double b0, double l0, double breadth, double length) {
    node.resizeRelocate(l0, b0, length, breadth);
  }

  @Override
  public void resize(Node node, double breadth, double length) {
    node.resize(length, breadth);
  }

  @Override
  public void relocate(Node node, double b0, double l0) {
    node.relocate(l0, b0);
  }

  @Override
  public Val<Double> widthEstimateProperty(Viewport<?, ?> content) {
    return content.totalLengthEstimateProperty();
  }

  @Override
  public Val<Double> heightEstimateProperty(Viewport<?, ?> content) {
    return content.totalBreadthEstimateProperty();
  }

  @Override
  public Var<Double> estimatedScrollXProperty(Viewport<?, ?> content) {
    return content.lengthOffsetEstimateProperty();
  }

  @Override
  public Var<Double> estimatedScrollYProperty(Viewport<?, ?> content) {
    return content.breadthOffsetProperty();
  }

  @Override
  public void scrollHorizontallyBy(Viewport<?, ?> content, double dx) {
    content.scrollLength(dx);
  }

  @Override
  public void scrollVerticallyBy(Viewport<?, ?> content, double dy) {
    content.scrollBreadth(dy);
  }

  @Override
  public void scrollHorizontallyToPixel(Viewport<?, ?> content, double pixel) {
    content.setLengthOffset(pixel);
  }

  @Override
  public void scrollVerticallyToPixel(Viewport<?, ?> content, double pixel) {
    content.setBreadthOffset(pixel);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> hitBeforeCells(double bOff, double lOff) {
    return ViewportHit.hitBeforeCells(lOff, bOff);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> hitAfterCells(double bOff, double lOff) {
    return ViewportHit.hitAfterCells(lOff, bOff);
  }

  @Override
  public <C extends Cell<?, ?>> ViewportHit<C> cellHit(int itemIndex, C cell, double bOff, double lOff) {
    return ViewportHit.cellHit(itemIndex, cell, lOff, bOff);
  }

}