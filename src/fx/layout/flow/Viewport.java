package fx.layout.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import fx.react.value.Val;
import fx.react.value.Var;
import fx.util.Lists;

/**
 * A VirtualFlow is a memory-efficient viewport that only renders enough of its content to completely fill up the
 * viewport through its {@link Navigator}. Based on the viewport's {@link Gravity}, it sequentially lays out the
 * {@link javafx.scene.Node}s of the {@link Cell}s until the viewport is completely filled up or it has no additional
 * cell's nodes to render.
 *
 * <p>
 *     Since this viewport does not fully render all of its content, the scroll values are estimates based on the nodes
 *     that are currently displayed in the viewport. If every node that could be rendered is the same width or same
 *     height, then the corresponding scroll values (e.g., scrollX or totalX) are accurate.
 *     <em>Note:</em> the VirtualFlow does not have scroll bars by default. These can be added by wrapping this object
 *     in a {@link VirtualizedScrollPane}.
 * </p>
 *
 * <p>
 *     Since the viewport can be used to lay out its content horizontally or vertically, it uses two
 *     orientation-agnostic terms to refer to its width and height: "breadth" and "length," respectively. The viewport
 *     always lays out its {@link Cell cell}'s {@link javafx.scene.Node}s from "top-to-bottom" or from "bottom-to-top"
 *     (these terms should be understood in reference to the viewport's {@link OrientationHelper orientation} and
 *     {@link Gravity}). Thus, its length ("height") is independent as the viewport's bounds are dependent upon
 *     its parent's bounds whereas its breadth ("width") is dependent upon its length.
 * </p>
 *
 * @param <T> the model content that the {@link Cell#getNode() cell's node} renders
 * @param <C> the {@link Cell} that can render the model with a {@link javafx.scene.Node}.
 */
public class Viewport<T, C extends Cell<T, ?>> extends Region implements Virtualized {

  /**
   * Creates a viewport that lays out content horizontally from left to right
   */
  public static <T, C extends Cell<T, ?>> Viewport<T, C> createHorizontal(ObservableList<T> items, Function<? super T, ? extends C> cellFactory) {
    return createHorizontal(items, cellFactory, Gravity.FRONT);
  }

  /**
   * Creates a viewport that lays out content horizontally
   */
  public static <T, C extends Cell<T, ?>> Viewport<T, C> createHorizontal(ObservableList<T> items, Function<? super T, ? extends C> cellFactory, Gravity gravity) {
    return new Viewport<>(items, cellFactory, new HorizontalHelper(), gravity);
  }

  /**
   * Creates a viewport that lays out content vertically from top to bottom
   */
  public static <T, C extends Cell<T, ?>> Viewport<T, C> createVertical(ObservableList<T> items, Function<? super T, ? extends C> cellFactory) {
    return createVertical(items, cellFactory, Gravity.FRONT);
  }

  /**
   * Creates a viewport that lays out content vertically from top to bottom
   */
  public static <T, C extends Cell<T, ?>> Viewport<T, C> createVertical(ObservableList<T> items, Function<? super T, ? extends C> cellFactory, Gravity gravity) {
    return new Viewport<>(items, cellFactory, new VerticalHelper(), gravity);
  }

  final ObservableList<T> items;
  final OrientationHelper orientation;
  final CellListManager<T, C> cellListManager;
  final SizeTracker sizeTracker;
  final CellPositioner<T, C> cellPositioner;
  final Navigator<T, C> navigator;

  final StyleableObjectProperty<Gravity> gravity =
    new StyleableObjectProperty<Gravity>() {
      @Override public Object getBean() { return Viewport.this; }
      @Override public String getName() { return "gravity"; }
      @Override public CssMetaData<? extends Styleable, Gravity> getCssMetaData() { return GRAVITY; }
    };

  // non-negative
  final Var<Double> breadthOffset0 = Var.newSimpleVar(0.0);
  final Var<Double> breadthOffset = breadthOffset0.asVar(this::setBreadthOffset);

  public Var<Double> breadthOffsetProperty() {
    return breadthOffset;
  }

  public Val<Double> totalBreadthEstimateProperty() {
    return sizeTracker.maxCellBreadthProperty();
  }

  final Var<Double> lengthOffsetEstimate;

  public Var<Double> lengthOffsetEstimateProperty() {
    return lengthOffsetEstimate;
  }

  Viewport(ObservableList<T> items, Function<? super T, ? extends C> cellFactory, OrientationHelper orientation, Gravity gravity) {
    this.getStyleClass().add("virtual-flow");
    this.items = items;
    this.orientation = orientation;
    this.cellListManager = new CellListManager<T, C>(this, items, cellFactory);
    this.gravity.set(gravity);
    var cells = cellListManager.getLazyCellList();
    this.sizeTracker = new SizeTracker(orientation, layoutBoundsProperty(), cells);
    this.cellPositioner = new CellPositioner<>(cellListManager, orientation, sizeTracker);
    this.navigator = new Navigator<>(cellListManager, cellPositioner, orientation, this.gravity, sizeTracker);

    getChildren().add(navigator);
    clipProperty().bind(Val.map(layoutBoundsProperty(), b -> new Rectangle(b.getWidth(), b.getHeight())));

    lengthOffsetEstimate = new StableBidirectionalVar<>(sizeTracker.lengthOffsetEstimateProperty(), this::setLengthOffset);

    // scroll content by mouse scroll
    this.addEventHandler(ScrollEvent.ANY, se -> {
      scrollXBy(-se.getDeltaX());
      scrollYBy(-se.getDeltaY());
      se.consume();
    });
  }

  public void dispose() {
    navigator.dispose();
    sizeTracker.dispose();
    cellListManager.dispose();
  }

  /**
   * If the item is out of view, instantiates a new cell for the item.
   * The returned cell will be properly sized, but not properly positioned
   * relative to the cells in the viewport, unless it is itself in the
   * viewport.
   *
   * @return Cell for the given item. The cell will be valid only until the
   * next layout pass. It should therefore not be stored. It is intended to
   * be used for measurement purposes only.
   */
  public C getCell(int itemIndex) {
    Lists.checkIndex(itemIndex, items.size());
    return cellPositioner.getSizedCell(itemIndex);
  }

  /**
   * This method calls {@link #layout()} as a side-effect to insure
   * that the VirtualFlow is up-to-date in light of any changes
   */
  public Optional<C> getCellIfVisible(int itemIndex) {
    // insure cells are up-to-date in light of any changes
    layout();
    return cellPositioner.getCellIfVisible(itemIndex);
  }

  /**
   * This method calls {@link #layout()} as a side-effect to insure
   * that the VirtualFlow is up-to-date in light of any changes
   */
  public ObservableList<C> visibleCells() {
    // insure cells are up-to-date in light of any changes
    layout();
    return cellListManager.getLazyCellList().memoizedItems();
  }

  public Val<Double> totalLengthEstimateProperty() {
    return sizeTracker.totalLengthEstimateProperty();
  }

  public Bounds cellToViewport(C cell, Bounds bounds) {
    return cell.getNode().localToParent(bounds);
  }

  public Point2D cellToViewport(C cell, Point2D point) {
    return cell.getNode().localToParent(point);
  }

  public Point2D cellToViewport(C cell, double x, double y) {
    return cell.getNode().localToParent(x, y);
  }

  @Override
  protected void layoutChildren() {
    // navigate to the target position and fill viewport
    for (;;) {
      var oldLayoutBreadth = sizeTracker.getCellLayoutBreadth();
      orientation.resize(navigator, oldLayoutBreadth, sizeTracker.getViewportLength());
      navigator.layout();
      if (oldLayoutBreadth == sizeTracker.getCellLayoutBreadth()) {
        break;
      }
    }

    var viewBreadth = orientation.breadth(this);
    var navigatorBreadth = orientation.breadth(navigator);
    var totalBreadth = breadthOffset0.getValue();
    var breadthDifference = navigatorBreadth - totalBreadth;
    if (breadthDifference < viewBreadth) {
      // viewport is scrolled all the way to the end of its breadth.
      //  but now viewport size (breadth) has increased
      var adjustment = viewBreadth - breadthDifference;
      orientation.relocate(navigator, -(totalBreadth - adjustment), 0);
      breadthOffset0.setValue(totalBreadth - adjustment);
    } else {
      orientation.relocate(navigator, -breadthOffset0.getValue(), 0);
    }
  }

  @Override
  protected final double computePrefWidth(double height) {
    return switch (getContentBias()) {
      case HORIZONTAL -> computePrefBreadth(); // vertical flow
      case VERTICAL -> computePrefLength(height); // horizontal flow
      default -> { throw new AssertionError("Unreachable code"); }
    };
  }

  @Override
  protected final double computePrefHeight(double width) {
    return switch (getContentBias()) {
      case HORIZONTAL -> computePrefLength(width); // vertical flow
      case VERTICAL -> computePrefBreadth(); // horizontal flow
      default -> { throw new AssertionError("Unreachable code"); }
    };
  }

  double computePrefBreadth() {
    return 100;
  }

  double computePrefLength(double breadth) {
    return 100;
  }

  @Override
  public final Orientation getContentBias() {
    return orientation.getContentBias();
  }

  void scrollLength(double deltaLength) {
    setLengthOffset(lengthOffsetEstimate.getValue() + deltaLength);
  }

  void scrollBreadth(double deltaBreadth) {
    setBreadthOffset(breadthOffset0.getValue() + deltaBreadth);
  }

  /**
   * Scroll the content horizontally by the given amount.
   *
   * @param deltaX positive value scrolls right, negative value scrolls left
   */
  @Override
  public void scrollXBy(double deltaX) {
    orientation.scrollHorizontallyBy(this, deltaX);
  }

  /**
   * Scroll the content vertically by the given amount.
   *
   * @param deltaY positive value scrolls down, negative value scrolls up
   */
  @Override
  public void scrollYBy(double deltaY) {
    orientation.scrollVerticallyBy(this, deltaY);
  }

  /**
   * Scroll the content horizontally to the pixel
   *
   * @param pixel - the pixel position to which to scroll
   */
  @Override
  public void scrollXToPixel(double pixel) {
    orientation.scrollHorizontallyToPixel(this, pixel);
  }

  /**
   * Scroll the content vertically to the pixel
   *
   * @param pixel - the pixel position to which to scroll
   */
  @Override
  public void scrollYToPixel(double pixel) {
    orientation.scrollVerticallyToPixel(this, pixel);
  }

  @Override
  public Val<Double> totalWidthEstimateProperty() {
    return orientation.widthEstimateProperty(this);
  }

  @Override
  public Val<Double> totalHeightEstimateProperty() {
    return orientation.heightEstimateProperty(this);
  }

  @Override
  public Var<Double> estimatedScrollXProperty() {
    return orientation.estimatedScrollXProperty(this);
  }

  @Override
  public Var<Double> estimatedScrollYProperty() {
    return orientation.estimatedScrollYProperty(this);
  }

  /**
   * Hits this virtual flow at the given coordinates.
   *
   * @param x x offset from the left edge of the viewport
   * @param y y offset from the top edge of the viewport
   * @return hit info containing the cell that was hit and coordinates
   * relative to the cell. If the hit was before the cells (i.e. above a
   * vertical flow content or left of a horizontal flow content), returns
   * a <em>hit before cells</em> containing offset from the top left corner
   * of the content. If the hit was after the cells (i.e. below a vertical
   * flow content or right of a horizontal flow content), returns a
   * <em>hit after cells</em> containing offset from the top right corner of
   * the content of a horizontal flow or bottom left corner of the content of
   * a vertical flow.
   */
  public ViewportHit<C> hit(double x, double y) {
    var bOff = orientation.getX(x, y);
    var lOff = orientation.getY(x, y);

    bOff += breadthOffset0.getValue();

    if (items.isEmpty()) {
      return orientation.hitAfterCells(bOff, lOff);
    }

    layout();

    var firstVisible = getFirstVisibleIndex();
    firstVisible = navigator.fillBackwardFrom0(firstVisible, lOff);
    var firstCell = cellPositioner.getVisibleCell(firstVisible);

    var lastVisible = getLastVisibleIndex();
    lastVisible = navigator.fillForwardFrom0(lastVisible, lOff);
    var lastCell = cellPositioner.getVisibleCell(lastVisible);

    if (lOff < orientation.minY(firstCell)) {
      return orientation.hitBeforeCells(bOff, lOff - orientation.minY(firstCell));
    } else if (lOff >= orientation.maxY(lastCell)) {
      return orientation.hitAfterCells(bOff, lOff - orientation.maxY(lastCell));
    } else {
      for (var i = firstVisible; i <= lastVisible; ++i) {
        var cell = cellPositioner.getVisibleCell(i);
        if (lOff < orientation.maxY(cell)) {
          return orientation.cellHit(i, cell, bOff, lOff - orientation.minY(cell));
        }
      }
      throw new AssertionError("unreachable code");
    }
  }

  /**
   * Forces the viewport to acts as though it scrolled from 0 to {@code viewportOffset}). <em>Note:</em> the
   * viewport makes an educated guess as to which cell is actually at {@code viewportOffset} if the viewport's
   * entire content was completely rendered.
   *
   * @param viewportOffset See {@link OrientationHelper} and its implementations for explanation on what the offset
   *                       means based on which implementation is used.
   */
  public void show(double viewportOffset) {
    if (viewportOffset < 0) {
      navigator.scrollCurrentPositionBy(viewportOffset);
    } else if (viewportOffset > sizeTracker.getViewportLength()) {
      navigator.scrollCurrentPositionBy(viewportOffset - sizeTracker.getViewportLength());
    } else {
      // do nothing, offset already in the viewport
    }
  }

  /**
   * Forces the viewport to show the given item by "scrolling" to it
   */
  public void show(int itemIdx) {
    navigator.setTargetPosition(new MinDistanceTo(itemIdx));
  }

  /**
   * Forces the viewport to show the given item as the first visible item as determined by its {@link Gravity}.
   */
  public void showAsFirst(int itemIdx) {
    navigator.setTargetPosition(new StartOffStart(itemIdx, 0.0));
  }

  /**
   * Forces the viewport to show the given item as the last visible item as determined by its {@link Gravity}.
   */
  public void showAsLast(int itemIdx) {
    navigator.setTargetPosition(new EndOffEnd(itemIdx, 0.0));
  }

  /**
   * Forces the viewport to show the given item by "scrolling" to it and then further "scrolling" by {@code offset}
   * in one layout call (e.g., this method does not "scroll" twice)
   *
   * @param offset the offset value as determined by the viewport's {@link OrientationHelper}.
   */
  public void showAtOffset(int itemIdx, double offset) {
    navigator.setTargetPosition(new StartOffStart(itemIdx, offset));
  }

  /**
   * Forces the viewport to show the given item by "scrolling" to it and then further "scrolling," so that the
   * {@code region} is visible, in one layout call (e.g., this method does not "scroll" twice).
   */
  public void show(int itemIndex, Bounds region) {
    navigator.showLengthRegion(itemIndex, orientation.minY(region), orientation.maxY(region));
    showBreadthRegion(orientation.minX(region), orientation.maxX(region));
  }

  /**
   * Get the index of the first visible cell (at the time of the last layout).
   *
   * @return The index of the first visible cell
   */
  public int getFirstVisibleIndex() {
    return navigator.getFirstVisibleIndex();
  }

  /**
   * Get the index of the last visible cell (at the time of the last layout).
   *
   * @return The index of the last visible cell
   */
  public int getLastVisibleIndex() {
    return navigator.getLastVisibleIndex();
  }

  void showBreadthRegion(double fromX, double toX) {
    var bOff = breadthOffset0.getValue();
    var spaceBefore = fromX - bOff;
    var spaceAfter = sizeTracker.getViewportBreadth() - toX + bOff;
    if (spaceBefore < 0 && spaceAfter > 0) {
      var shift = Math.min(-spaceBefore, spaceAfter);
      setBreadthOffset(bOff - shift);
    } else if (spaceAfter < 0 && spaceBefore > 0) {
      var shift = Math.max(spaceAfter, -spaceBefore);
      setBreadthOffset(bOff - shift);
    }
  }

  void setLengthOffset(double pixels) {
    var total = totalLengthEstimateProperty().getOrElse(0.0);
    var length = sizeTracker.getViewportLength();
    var max = Math.max(total - length, 0);
    var current = lengthOffsetEstimate.getValue();

    if (pixels > max) {
      pixels = max;
    }
    if (pixels < 0) {
      pixels = 0;
    }
    var diff = pixels - current;
    if (diff == 0) {
      // do nothing
    } else if (Math.abs(diff) <= length) { // distance less than one screen
      navigator.scrollCurrentPositionBy(diff);
    } else {
      jumpToAbsolutePosition(pixels);
    }
  }

  void setBreadthOffset(double pixels) {
    var total = totalBreadthEstimateProperty().getValue();
    var breadth = sizeTracker.getViewportBreadth();
    var max = Math.max(total - breadth, 0);
    var current = breadthOffset0.getValue();

    if (pixels > max) {
      pixels = max;
    }
    if (pixels < 0) {
      pixels = 0;
    }
    if (pixels != current) {
      breadthOffset0.setValue(pixels);
      requestLayout();
      // TODO: could be safely relocated right away?
      // (Does relocation request layout?)
    }
  }

  void jumpToAbsolutePosition(double pixels) {
    if (items.isEmpty()) {
      return;
    }
    // guess the first visible cell and its offset in the viewport
    var avgLen = sizeTracker.getAverageLengthEstimate().orElse(0.0);
    if (avgLen == 0.0) {
      return;
    }
    var first = (int) Math.floor(pixels / avgLen);
    var firstOffset = -(pixels % avgLen);
    if (first < items.size()) {
      navigator.setTargetPosition(new StartOffStart(first, firstOffset));
    } else {
      navigator.setTargetPosition(new EndOffEnd(items.size() - 1, 0.0));
    }
  }

  /**
   * The gravity of the virtual flow.  When there are not enough cells to fill
   * the full height (vertical virtual flow) or width (horizontal virtual flow),
   * the cells are placed either at the front (vertical: top, horizontal: left),
   * or rear (vertical: bottom, horizontal: right) of the virtual flow, depending
   * on the value of the gravity property.
   *
   * The gravity can also be styled in CSS, using the "-flowless-gravity" property,
   * for example:
   * <pre>.virtual-flow { -flowless-gravity: rear; }</pre>
   */
  public ObjectProperty<Gravity> gravityProperty() {
    return gravity;
  }

  public Gravity getGravity() {
    return gravity.get();
  }

  public void setGravity(Gravity gravity) {
    this.gravity.set(gravity);
  }

  @SuppressWarnings({"rawtypes","unchecked"}) // Because of the cast we have to perform, below
  static final CssMetaData<Viewport, Gravity> GRAVITY =
    new CssMetaData<Viewport, Gravity>(
      "-flowless-gravity",
      // JavaFX seems to have an odd return type on getEnumConverter: "? extends Enum<?>", not E as the second generic type.
      // Even though if you look at the source, the EnumConverter type it uses does have the type E.
      // To get round this, we cast on return:
      (StyleConverter<?, Gravity>) StyleConverter.getEnumConverter(Gravity.class), Gravity.FRONT) {

      @Override
      public boolean isSettable(Viewport virtualFlow) {
        return !virtualFlow.gravity.isBound();
      }
      @Override
      public StyleableProperty<Gravity> getStyleableProperty(Viewport virtualFlow) {
        return virtualFlow.gravity;
      }
    };

  static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
  /*<init>*/ static {
    List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Region.getClassCssMetaData());
    styleables.add(GRAVITY);
    STYLEABLES = Collections.unmodifiableList(styleables);
  }

  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
    return STYLEABLES;
  }

  @Override
  public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
    return getClassCssMetaData();
  }

}
