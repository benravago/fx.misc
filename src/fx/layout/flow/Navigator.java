package fx.layout.flow;

import java.util.OptionalInt;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

import fx.react.Subscription;
import fx.react.collection.LiveList;
import fx.react.collection.MemoizedList;
import fx.react.collection.QuasiChange;

/**
 * Responsible for laying out cells' nodes within the viewport based on a single anchor node. In a layout call,
 * this anchor node is positioned in the viewport before any other node and then nodes are positioned above and
 * below that anchor node sequentially. This sequential layout continues until the viewport's "top" and "bottom" edges
 * are reached or there are no other cells' nodes to render. In this latter case (when there is not enough content to
 * fill up the entire viewport), the displayed cells are repositioned towards the "ground," based on the
 * {@link Viewport}'s {@link Gravity} value, and any remaining unused space counts as the "sky."
 */
class Navigator<T, C extends Cell<T, ?>> extends Region implements TargetPositionVisitor {

  final CellListManager<T, C> cellListManager;
  final MemoizedList<C> cells;
  final CellPositioner<T, C> positioner;
  final OrientationHelper orientation;
  final ObjectProperty<Gravity> gravity;
  final SizeTracker sizeTracker;
  final Subscription itemsSubscription;

  TargetPosition currentPosition = TargetPosition.BEGINNING;
  TargetPosition targetPosition = TargetPosition.BEGINNING;
  int firstVisibleIndex = -1;
  int lastVisibleIndex = -1;

  Navigator(CellListManager<T, C> cellListManager, CellPositioner<T, C> positioner, OrientationHelper orientation, ObjectProperty<Gravity> gravity, SizeTracker sizeTracker) {
    this.cellListManager = cellListManager;
    this.cells = cellListManager.getLazyCellList();
    this.positioner = positioner;
    this.orientation = orientation;
    this.gravity = gravity;
    this.sizeTracker = sizeTracker;

    this.itemsSubscription = LiveList.observeQuasiChanges(cellListManager.getLazyCellList(), this::itemsChanged);
    Bindings.bindContent(getChildren(), cellListManager.getNodes());
    // When gravity changes, we must redo our layout:
    gravity.addListener((prop, oldVal, newVal) -> requestLayout());
  }

  void dispose() {
    itemsSubscription.unsubscribe();
    Bindings.unbindContent(getChildren(), cellListManager.getNodes());
  }

  @Override
  protected void layoutChildren() {
    // invalidate breadth for each cell that has dirty layout
    var n = cells.getMemoizedCount();
    for (var i = 0; i < n; ++i) {
      var j = cells.indexOfMemoizedItem(i);
      var node = cells.get(j).getNode();
      if (node instanceof Parent && ((Parent) node).isNeedsLayout()) {
        sizeTracker.forgetSizeOf(j);
      }
    }
    if (!cells.isEmpty()) {
      targetPosition.clamp(cells.size()).accept(this);
    }
    currentPosition = getCurrentPosition();
    targetPosition = currentPosition;
  }

  /**
   * Sets the {@link TargetPosition} used to layout the anchor node and re-lays out the viewport
   */
  void setTargetPosition(TargetPosition targetPosition) {
    this.targetPosition = targetPosition;
    requestLayout();
  }

  /**
   * Sets the {@link TargetPosition} used to layout the anchor node to the current position scrolled by {@code delta}
   * and re-lays out the viewport
   */
  void scrollCurrentPositionBy(double delta) {
    targetPosition = currentPosition.scrollBy(delta);
    requestLayout();
  }

  TargetPosition getCurrentPosition() {
    if (cellListManager.getLazyCellList().getMemoizedCount() == 0) {
      return TargetPosition.BEGINNING;
    } else {
      var cell = positioner.getVisibleCell(firstVisibleIndex);
      return new StartOffStart(firstVisibleIndex, orientation.minY(cell));
    }
  }

  void itemsChanged(QuasiChange<?> ch) {
    for (var mod : ch) {
      targetPosition = targetPosition.transformByChange(mod.getFrom(), mod.getRemovedSize(), mod.getAddedSize());
    }
    requestLayout();
    // TODO: could optimize to only request layout if target position changed or cells in the viewport are affected
  }

  void showLengthRegion(int itemIndex, double fromY, double toY) {
    setTargetPosition(new MinDistanceTo(itemIndex, Offset.fromStart(fromY), Offset.fromStart(toY)));
  }

  @Override
  public void visit(StartOffStart targetPosition) {
    cropToNeighborhoodOf(targetPosition.itemIndex); // Fix for issue #70 (!)
    positioner.placeStartAt(targetPosition.itemIndex, targetPosition.offsetFromStart);
    fillViewportFrom(targetPosition.itemIndex);
  }

  @Override
  public void visit(EndOffEnd targetPosition) {
    cropToNeighborhoodOf(targetPosition.itemIndex); // Related to issue #70 (?)
    positioner.placeEndFromEnd(targetPosition.itemIndex, targetPosition.offsetFromEnd);
    fillViewportFrom(targetPosition.itemIndex);
  }

  void cropToNeighborhoodOf(int itemIndex) {
    var begin = Math.max(0, getFirstVisibleIndex());
    var end = Math.max(itemIndex, getLastVisibleIndex());
    positioner.cropTo(Math.min(begin, itemIndex), end + 1);
  }

  @Override
  public void visit(MinDistanceTo targetPosition) {
    var cell = positioner.getCellIfVisible(targetPosition.itemIndex);
    if (cell.isPresent()) {
      placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
    } else {
      OptionalInt prevVisible;
      OptionalInt nextVisible;
      if ((prevVisible = positioner.lastVisibleBefore(targetPosition.itemIndex)).isPresent()) {
        // Try keeping prevVisible in place:
        // fill the viewport, see if the target item appeared.
        fillForwardFrom(prevVisible.getAsInt());
        cell = positioner.getCellIfVisible(targetPosition.itemIndex);
        if (cell.isPresent()) {
          placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
        } else if (targetPosition.maxY.isFromStart()) {
          placeStartOffEndMayCrop(targetPosition.itemIndex, -targetPosition.maxY.getValue());
        } else {
          placeEndOffEndMayCrop(targetPosition.itemIndex, -targetPosition.maxY.getValue());
        }
      } else if ((nextVisible = positioner.firstVisibleAfter(targetPosition.itemIndex + 1)).isPresent()) {
        // Try keeping nextVisible in place:
        // fill the viewport, see if the target item appeared.
        fillBackwardFrom(nextVisible.getAsInt());
        cell = positioner.getCellIfVisible(targetPosition.itemIndex);
        if (cell.isPresent()) {
          placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
        } else if (targetPosition.minY.isFromStart()) {
          placeStartAtMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
        } else {
          placeEndOffStartMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
        }
      } else {
        if (targetPosition.minY.isFromStart()) {
          placeStartAtMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
        } else {
          placeEndOffStartMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
        }
      }
    }
    fillViewportFrom(targetPosition.itemIndex);
  }

  /**
   * Get the index of the first visible cell (at the time of the last layout).
   *
   * @return The index of the first visible cell
   */
  int getFirstVisibleIndex() {
    return firstVisibleIndex;
  }

  /**
   * Get the index of the last visible cell (at the time of the last layout).
   *
   * @return The index of the last visible cell
   */
  int getLastVisibleIndex() {
    return lastVisibleIndex;
  }

  void placeToViewport(int itemIndex, Offset from, Offset to) {
    var cell = positioner.getVisibleCell(itemIndex);
    var fromY = from.isFromStart() ? from.getValue() : orientation.length(cell) + to.getValue();
    var toY = to.isFromStart() ? to.getValue() : orientation.length(cell) + to.getValue();
    placeToViewport(itemIndex, fromY, toY);
  }

  void placeToViewport(int itemIndex, double fromY, double toY) {
    var cell = positioner.getVisibleCell(itemIndex);
    var d = positioner.shortestDeltaToViewport(cell, fromY, toY);
    positioner.placeStartAt(itemIndex, orientation.minY(cell) + d);
  }

  void placeStartAtMayCrop(int itemIndex, double startOffStart) {
    cropToNeighborhoodOf(itemIndex, startOffStart);
    positioner.placeStartAt(itemIndex, startOffStart);
  }

  void placeStartOffEndMayCrop(int itemIndex, double startOffEnd) {
    cropToNeighborhoodOf(itemIndex, startOffEnd);
    positioner.placeStartFromEnd(itemIndex, startOffEnd);
  }

  void placeEndOffStartMayCrop(int itemIndex, double endOffStart) {
    cropToNeighborhoodOf(itemIndex, endOffStart);
    positioner.placeEndFromStart(itemIndex, endOffStart);
  }

  void placeEndOffEndMayCrop(int itemIndex, double endOffEnd) {
    cropToNeighborhoodOf(itemIndex, endOffEnd);
    positioner.placeEndFromEnd(itemIndex, endOffEnd);
  }

  void cropToNeighborhoodOf(int itemIndex, double additionalOffset) {
    var spaceBefore = Math.max(0, sizeTracker.getViewportLength() + additionalOffset);
    var spaceAfter = Math.max(0, sizeTracker.getViewportLength() - additionalOffset);
    var avgLen = sizeTracker.getAverageLengthEstimate();
    int itemsBefore = avgLen.map(l -> spaceBefore / l).orElse(5.0).intValue();
    int itemsAfter = avgLen.map(l -> spaceAfter / l).orElse(5.0).intValue();
    positioner.cropTo(itemIndex - itemsBefore, itemIndex + 1 + itemsAfter);
  }

  int fillForwardFrom(int itemIndex) {
    return fillForwardFrom(itemIndex, sizeTracker.getViewportLength());
  }

  int fillForwardFrom0(int itemIndex) {
    return fillForwardFrom0(itemIndex, sizeTracker.getViewportLength());
  }

  int fillForwardFrom(int itemIndex, double upTo) {
    // resize and/or reposition the starting cell
    // in case the preferred or available size changed
    var cell = positioner.getVisibleCell(itemIndex);
    var length0 = orientation.minY(cell);
    positioner.placeStartAt(itemIndex, length0);
    return fillForwardFrom0(itemIndex, upTo);
  }

  int fillForwardFrom0(int itemIndex, double upTo) {
    var max = orientation.maxY(positioner.getVisibleCell(itemIndex));
    var i = itemIndex;
    while (max < upTo && i < cellListManager.getLazyCellList().size() - 1) {
      ++i;
      var c = positioner.placeStartAt(i, max);
      max = orientation.maxY(c);
    }
    return i;
  }

  int fillBackwardFrom(int itemIndex) {
    return fillBackwardFrom(itemIndex, 0.0);
  }

  int fillBackwardFrom0(int itemIndex) {
    return fillBackwardFrom0(itemIndex, 0.0);
  }

  int fillBackwardFrom(int itemIndex, double upTo) {
    // resize and/or reposition the starting cell
    // in case the preferred or available size changed
    var cell = positioner.getVisibleCell(itemIndex);
    var length0 = orientation.minY(cell);
    positioner.placeStartAt(itemIndex, length0);
    return fillBackwardFrom0(itemIndex, upTo);
  }

  // does not re-place the anchor cell
  int fillBackwardFrom0(int itemIndex, double upTo) {
    var min = orientation.minY(positioner.getVisibleCell(itemIndex));
    var i = itemIndex;
    while (min > upTo && i > 0) {
      --i;
      var c = positioner.placeEndFromStart(i, min);
      min = orientation.minY(c);
    }
    return i;
  }

  /**
   * Starting from the anchor cell's node, fills the viewport from the anchor to the "ground" and then from the anchor
   * to the "sky".
   *
   * @param itemIndex the index of the anchor cell
   */
  void fillViewportFrom(int itemIndex) {
    /* cell for itemIndex is assumed to be placed correctly */
    // fill up to the ground
    var ground = fillTowardsGroundFrom0(itemIndex);
    // if ground not reached, shift cells to the ground
    var gapBefore = distanceFromGround(ground);
    if (gapBefore > 0) {
      shiftCellsTowardsGround(ground, itemIndex, gapBefore);
    }
    // fill up to the sky
    var sky = fillTowardsSkyFrom0(itemIndex);
    // if sky not reached, add more cells under the ground and then shift
    var gapAfter = distanceFromSky(sky);
    if (gapAfter > 0) {
      ground = fillTowardsGroundFrom0(ground, -gapAfter);
      var extraBefore = -distanceFromGround(ground);
      var shift = Math.min(gapAfter, extraBefore);
      shiftCellsTowardsGround(ground, sky, -shift);
    }
    // crop to the visible cells
    var first = Math.min(ground, sky);
    var last = Math.max(ground, sky);
    while (first < last && orientation.maxY(positioner.getVisibleCell(first)) <= 0.0) {
      ++first;
    }
    while (last > first && orientation.minY(positioner.getVisibleCell(last)) >= sizeTracker.getViewportLength()) {
      --last;
    }
    firstVisibleIndex = first;
    lastVisibleIndex = last;
    positioner.cropTo(first, last + 1);
  }

  int fillTowardsGroundFrom0(int itemIndex) {
    return gravity.get() == Gravity.FRONT ? fillBackwardFrom0(itemIndex) : fillForwardFrom0(itemIndex);
  }

  int fillTowardsGroundFrom0(int itemIndex, double upTo) {
    return gravity.get() == Gravity.FRONT
      ? fillBackwardFrom0(itemIndex, upTo)
      : fillForwardFrom0(itemIndex, sizeTracker.getViewportLength() - upTo);
  }

  int fillTowardsSkyFrom0(int itemIndex) {
    return gravity.get() == Gravity.FRONT
      ? fillForwardFrom0(itemIndex)
      : fillBackwardFrom0(itemIndex);
  }

  double distanceFromGround(int itemIndex) {
    var cell = positioner.getVisibleCell(itemIndex);
    return gravity.get() == Gravity.FRONT
      ? orientation.minY(cell)
      : sizeTracker.getViewportLength() - orientation.maxY(cell);
  }

  double distanceFromSky(int itemIndex) {
    var cell = positioner.getVisibleCell(itemIndex);
    return gravity.get() == Gravity.FRONT
      ? sizeTracker.getViewportLength() - orientation.maxY(cell)
      : orientation.minY(cell);
  }

  void shiftCellsTowardsGround(int groundCellIndex, int lastCellIndex, double amount) {
    if (gravity.get() == Gravity.FRONT) {
      assert groundCellIndex <= lastCellIndex;
      for (var i = groundCellIndex; i <= lastCellIndex; ++i) {
        positioner.shiftCellBy(positioner.getVisibleCell(i), -amount);
      }
    } else {
      assert groundCellIndex >= lastCellIndex;
      for (var i = groundCellIndex; i >= lastCellIndex; --i) {
        positioner.shiftCellBy(positioner.getVisibleCell(i), amount);
      }
    }
  }

}
