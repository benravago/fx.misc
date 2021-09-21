package fx.layout.flow;

import javafx.geometry.Point2D;

class CellHit<C extends Cell<?, ?>> extends ViewportHit<C> {

  final int cellIdx;
  final C cell;
  final Point2D cellOffset;

  CellHit(int cellIdx, C cell, Point2D cellOffset) {
    this.cellIdx = cellIdx;
    this.cell = cell;
    this.cellOffset = cellOffset;
  }

  @Override
  public boolean isCellHit() {
    return true;
  }

  @Override
  public boolean isBeforeCells() {
    return false;
  }

  @Override
  public boolean isAfterCells() {
    return false;
  }

  @Override
  public int getCellIndex() {
    return cellIdx;
  }

  @Override
  public C getCell() {
    return cell;
  }

  @Override
  public Point2D getCellOffset() {
    return cellOffset;
  }

  @Override
  public Point2D getOffsetBeforeCells() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point2D getOffsetAfterCells() {
    throw new UnsupportedOperationException();
  }

}