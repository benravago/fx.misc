package fx.layout.flow;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;

import fx.react.EventStreams;
import fx.react.Subscription;
import fx.react.collection.MemoizedList;
import fx.react.value.Val;
import fx.react.value.ValBase;

/**
 * Estimates the size of the entire viewport (if it was actually completely rendered) based on the known sizes of the
 * {@link Cell}s whose nodes are currently displayed in the viewport and an estimated average of
 * {@link Cell}s whose nodes are not displayed in the viewport. The meaning of {@link #breadthForCells} and
 * {@link #totalLengthEstimate} are dependent upon which implementation of {@link OrientationHelper} is used.
 */
class SizeTracker {

  final OrientationHelper orientation;
  final ObservableObjectValue<Bounds> viewportBounds;
  final MemoizedList<? extends Cell<?, ?>> cells;

  final MemoizedList<Double> breadths;
  final Val<Double> maxKnownMinBreadth;

  /** Stores either the greatest minimum cell's node's breadth or the viewport's breadth */
  final Val<Double> breadthForCells;

  final MemoizedList<Double> lengths;

  /** Stores either null or the average length of the cells' nodes currently displayed in the viewport */
  final Val<Double> averageLengthEstimate;

  final Val<Double> totalLengthEstimate;
  final Val<Double> lengthOffsetEstimate;

  final Subscription subscription;

  /**
   * Constructs a SizeTracker
   *
   * @param orientation if vertical, breadth = width and length = height;
   *                    if horizontal, breadth = height and length = width
   */
  SizeTracker(OrientationHelper orientation, ObservableObjectValue<Bounds> viewportBounds, MemoizedList<? extends Cell<?, ?>> lazyCells) {
    this.orientation = orientation;
    this.viewportBounds = viewportBounds;
    this.cells = lazyCells;
    this.breadths = lazyCells.map(orientation::minBreadth).memoize();
    this.maxKnownMinBreadth = breadths.memoizedItems().reduce(Math::max).orElseConst(0.0);

    this.breadthForCells = Val.combine(
      maxKnownMinBreadth,
      viewportBounds,
      (a, b) -> Math.max(a, orientation.breadth(b))
    );

    Val<Function<Cell<?, ?>, Double>> lengthFn =
      (orientation instanceof HorizontalHelper ? breadthForCells : avoidFalseInvalidations(breadthForCells))
      .map(breadth -> cell -> orientation.prefLength(cell, breadth));

    this.lengths = cells.mapDynamic(lengthFn).memoize();

    var knownLengths = this.lengths.memoizedItems();
    var sumOfKnownLengths = knownLengths.reduce((a, b) -> a + b).orElseConst(0.0);
    var knownLengthCount = knownLengths.sizeProperty();

    this.averageLengthEstimate =
      Val.create(() -> {
        // make sure to use pref lengths of all present cells
        for (var i = 0; i < cells.getMemoizedCount(); ++i) {
          var j = cells.indexOfMemoizedItem(i);
          lengths.force(j, j + 1);
        }
        var count = knownLengthCount.getValue();
        return count == 0 ? null : sumOfKnownLengths.getValue() / count;
      }, sumOfKnownLengths, knownLengthCount);

    this.totalLengthEstimate =
      Val.combine(
        averageLengthEstimate,
        cells.sizeProperty(),
        (avg, n) -> n * avg
    );

    Val<Integer> firstVisibleIndex =
      Val.create(() ->
        cells.getMemoizedCount() == 0 ? null : cells.indexOfMemoizedItem(0), cells, cells.memoizedItems()
      ); // need to observe cells.memoizedItems()
         // as well, because they may change without a change in cells.

    var firstVisibleCell =
      cells
        .memoizedItems()
        .collapse(visCells -> visCells.isEmpty() ? null : visCells.get(0));

    var knownLengthCountBeforeFirstVisibleCell =
      Val.create(() -> {
        return firstVisibleIndex.getOpt().map(i -> lengths.getMemoizedCountBefore(Math.min(i, lengths.size()))).orElse(0);
      }, lengths, firstVisibleIndex);

    var totalKnownLengthBeforeFirstVisibleCell =
      knownLengths
        .reduceRange(knownLengthCountBeforeFirstVisibleCell.map(n -> new IndexRange(0, n)), (a, b) -> a + b)
        .orElseConst(0.0);

    var unknownLengthEstimateBeforeFirstVisibleCell =
      Val.combine(
        firstVisibleIndex,
        knownLengthCountBeforeFirstVisibleCell,
        averageLengthEstimate,
        (firstIdx, knownCnt, avgLen) -> (firstIdx - knownCnt) * avgLen
      );

    var firstCellMinY =
      firstVisibleCell.flatMap(orientation::minYProperty);

    this.lengthOffsetEstimate =
      Val.wrap(
        EventStreams.combine(
          totalKnownLengthBeforeFirstVisibleCell.values(), // a
          unknownLengthEstimateBeforeFirstVisibleCell.values(), // b
          firstCellMinY.values() // c = minY
        )
        .filter(t -> t.a() != null && t.b() != null && t.c() != null)
        .thenRetainLatestFor(Duration.ofMillis(1))
        .map(t -> Double.valueOf(t.a() + t.b() - t.c()))
        .toBinding(0.0)
      );

    // pinning totalLengthEstimate and lengthOffsetEstimate
    // binds it all together and enables memoization
    this.subscription = Subscription.multi(totalLengthEstimate.pin(), lengthOffsetEstimate.pin());
  }

  static <T> Val<T> avoidFalseInvalidations(Val<T> src) {
    return new ValBase<T>() {
      @Override
      protected Subscription connect() {
        return src.observeChanges((obs, oldVal, newVal) -> invalidate());
      }

      @Override
      protected T computeValue() {
        return src.getValue();
      }
    };
  }

  void dispose() {
    subscription.unsubscribe();
  }

  Val<Double> maxCellBreadthProperty() {
    return maxKnownMinBreadth;
  }

  double getViewportBreadth() {
    return orientation.breadth(viewportBounds.get());
  }

  double getViewportLength() {
    return orientation.length(viewportBounds.get());
  }

  Val<Double> averageLengthEstimateProperty() {
    return averageLengthEstimate;
  }

  Optional<Double> getAverageLengthEstimate() {
    return averageLengthEstimate.getOpt();
  }

  Val<Double> totalLengthEstimateProperty() {
    return totalLengthEstimate;
  }

  Val<Double> lengthOffsetEstimateProperty() {
    return lengthOffsetEstimate;
  }

  double breadthFor(int itemIndex) {
    assert cells.isMemoized(itemIndex);
    breadths.force(itemIndex, itemIndex + 1);
    return breadthForCells.getValue();
  }

  void forgetSizeOf(int itemIndex) {
    breadths.forget(itemIndex, itemIndex + 1);
    lengths.forget(itemIndex, itemIndex + 1);
  }

  double lengthFor(int itemIndex) {
    return lengths.get(itemIndex);
  }

  double getCellLayoutBreadth() {
    return breadthForCells.getValue();
  }

}
