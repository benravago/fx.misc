package fx.layout.flow;

import fx.react.value.Val;
import fx.react.value.Var;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;

/**
 * Helper class for returning the correct value (should the {@code width} or {@code height} be returned?) or calling
 * the correct method (should {@code setWidth(args)} or {@code setHeight(args)}, so that one one class can be used
 * instead of a generic with two implementations. See its implementations for more details ({@link VerticalHelper}
 * and {@link HorizontalHelper}) on what "layoutX", "layoutY", and "viewport offset" values represent.
 */
interface OrientationHelper {

  Orientation getContentBias();

  double getX(double x, double y);

  double getY(double x, double y);

  double length(Bounds bounds);

  double breadth(Bounds bounds);

  double minX(Bounds bounds);

  double minY(Bounds bounds);

  default double maxX(Bounds bounds) {
    return minX(bounds) + breadth(bounds);
  }

  default double maxY(Bounds bounds) {
    return minY(bounds) + length(bounds);
  }

  double layoutX(Node node);

  double layoutY(Node node);

  DoubleProperty layoutYProperty(Node node);

  default double length(Node node) {
    return length(node.getLayoutBounds());
  }

  default double breadth(Node node) {
    return breadth(node.getLayoutBounds());
  }

  default Val<Double> minYProperty(Node node) {
    return Val.combine(
      layoutYProperty(node),
      node.layoutBoundsProperty(),
      (layoutY, layoutBounds) -> layoutY.doubleValue() + minY(layoutBounds)
    );
  }

  default double minY(Node node) {
    return layoutY(node) + minY(node.getLayoutBounds());
  }

  default double maxY(Node node) {
    return minY(node) + length(node);
  }

  default double minX(Node node) {
    return layoutX(node) + minX(node.getLayoutBounds());
  }

  default double maxX(Node node) {
    return minX(node) + breadth(node);
  }

  default double length(Cell<?, ?> cell) {
    return length(cell.getNode());
  }

  default double breadth(Cell<?, ?> cell) {
    return breadth(cell.getNode());
  }

  default Val<Double> minYProperty(Cell<?, ?> cell) {
    return minYProperty(cell.getNode());
  }

  default double minY(Cell<?, ?> cell) {
    return minY(cell.getNode());
  }

  default double maxY(Cell<?, ?> cell) {
    return maxY(cell.getNode());
  }

  default double minX(Cell<?, ?> cell) {
    return minX(cell.getNode());
  }

  default double maxX(Cell<?, ?> cell) {
    return maxX(cell.getNode());
  }

  double minBreadth(Node node);

  default double minBreadth(Cell<?, ?> cell) {
    return minBreadth(cell.getNode());
  }

  double prefBreadth(Node node);

  double prefLength(Node node, double breadth);

  default double prefLength(Cell<?, ?> cell, double breadth) {
    return prefLength(cell.getNode(), breadth);
  }

  void resizeRelocate(Node node, double b0, double l0, double breadth, double length);

  void resize(Node node, double breadth, double length);

  void relocate(Node node, double b0, double l0);

  default void resize(Cell<?, ?> cell, double breadth, double length) {
    resize(cell.getNode(), breadth, length);
  }

  default void relocate(Cell<?, ?> cell, double b0, double l0) {
    relocate(cell.getNode(), b0, l0);
  }

  Val<Double> widthEstimateProperty(Viewport<?, ?> content);

  Val<Double> heightEstimateProperty(Viewport<?, ?> content);

  Var<Double> estimatedScrollXProperty(Viewport<?, ?> content);

  Var<Double> estimatedScrollYProperty(Viewport<?, ?> content);

  void scrollHorizontallyBy(Viewport<?, ?> content, double dx);

  void scrollVerticallyBy(Viewport<?, ?> content, double dy);

  void scrollHorizontallyToPixel(Viewport<?, ?> content, double pixel);

  void scrollVerticallyToPixel(Viewport<?, ?> content, double pixel);

  <C extends Cell<?, ?>> ViewportHit<C> hitBeforeCells(double bOff, double lOff);

  <C extends Cell<?, ?>> ViewportHit<C> hitAfterCells(double bOff, double lOff);

  <C extends Cell<?, ?>> ViewportHit<C> cellHit(int itemIndex, C cell, double bOff, double lOff);

}