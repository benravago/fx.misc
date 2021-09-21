package fx.layout.flow;

import java.util.Optional;
import java.util.function.Function;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;

import fx.react.EventStreams;
import fx.react.Subscription;
import fx.react.collection.LiveList;
import fx.react.collection.MemoizedList;
import fx.react.collection.QuasiModification;

/**
 * Tracks all of the cells that the viewport can display ({@link #cells}) and which cells the viewport is currently
 * displaying ({@link #presentCells}).
 */
class CellListManager<T, C extends Cell<T, ? extends Node>> {

  final Node owner;
  final CellPool<T, C> cellPool;
  final MemoizedList<C> cells;
  final LiveList<C> presentCells;
  final LiveList<Node> cellNodes;

  final Subscription presentCellsSubscription;

  CellListManager(Node owner, ObservableList<T> items, Function<? super T, ? extends C> cellFactory) {
    this.owner = owner;
    this.cellPool = new CellPool<>(cellFactory);
    this.cells = LiveList.map(items, this::cellForItem).memoize();
    this.presentCells = cells.memoizedItems();
    this.cellNodes = presentCells.map(Cell::getNode);
    this.presentCellsSubscription = presentCells.observeQuasiModifications(this::presentCellsChanged);
  }

  void dispose() {
    // return present cells to pool *before* unsubscribing,
    // because stopping to observe memoized items may clear memoized items
    presentCells.forEach(cellPool::acceptCell);
    presentCellsSubscription.unsubscribe();
    cellPool.dispose();
  }

  /** Gets the list of nodes that the viewport is displaying */
  ObservableList<Node> getNodes() {
    return cellNodes;
  }

  MemoizedList<C> getLazyCellList() {
    return cells;
  }

  boolean isCellPresent(int itemIndex) {
    return cells.isMemoized(itemIndex);
  }

  C getPresentCell(int itemIndex) {
    // both getIfMemoized() and get() may throw
    return cells.getIfMemoized(itemIndex).get();
  }

  Optional<C> getCellIfPresent(int itemIndex) {
    return (itemIndex >= cells.size() || itemIndex < 0) ? Optional.empty() : cells.getIfMemoized(itemIndex); // getIfMemoized() may throw
  }

  C getCell(int itemIndex) {
    return cells.get(itemIndex);
  }

  /**
   * Updates the list of cells to display
   *
   * @param fromItem the index of the first item to display
   * @param toItem the index of the last item to display
   */
  void cropTo(int fromItem, int toItem) {
    fromItem = Math.max(fromItem, 0);
    toItem = Math.max(Math.min(toItem, cells.size()), 0);
    var memorizedRange = cells.getMemoizedItemsRange();
    if (memorizedRange.getStart() < fromItem) {
      cells.forget(0, fromItem);
    }
    if (memorizedRange.getEnd() > toItem) {
      cells.forget(toItem, cells.size());
    }
  }

  C cellForItem(T item) {
    var cell = cellPool.getCell(item);
    // apply CSS when the cell is first added to the scene
    var node = cell.getNode();
    EventStreams.nonNullValuesOf(node.sceneProperty()).subscribeForOne(scene -> node.applyCss());
    // Make cell initially invisible.
    // It will be made visible when it is positioned.
    node.setVisible(false);
    if (cell.isReusable()) {
      // if cell is reused i think adding event handler
      // would cause resource leakage.
      node.setOnScroll(this::pushScrollEvent);
      node.setOnScrollStarted(this::pushScrollEvent);
      node.setOnScrollFinished(this::pushScrollEvent);
    } else {
      node.addEventHandler(ScrollEvent.ANY, this::pushScrollEvent);
    }
    return cell;
  }

  /**
   * Push scroll events received by cell nodes directly to
   * the 'owner' Node. (Generally likely to be a VirtualFlow
   * but not required.)
   *
   * Normal bubbling of scroll events gets interrupted during
   * a scroll gesture when the Cell's Node receiving the event
   * has moved out of the viewport and is thus removed from
   * the Navigator's children list. This breaks expected trackpad
   * scrolling behaviour, at least on macOS.
   *
   * So here we take over event-bubbling duties for ScrollEvent
   * and push them ourselves directly to the given owner.
   */
  void pushScrollEvent(ScrollEvent se) {
    owner.fireEvent(se);
    se.consume();
  }

  void presentCellsChanged(QuasiModification<? extends C> mod) {
    // add removed cells back to the pool
    for (var cell : mod.getRemoved()) {
      cellPool.acceptCell(cell);
    }
    // update indices of added cells and cells after the added cells
    for (int i = mod.getFrom(); i < presentCells.size(); ++i) {
      presentCells.get(i).updateIndex(cells.indexOfMemoizedItem(i));
    }
  }

}
