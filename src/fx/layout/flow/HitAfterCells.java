package fx.layout.flow;

import javafx.geometry.Point2D;

class HitAfterCells<C extends Cell<?, ?>> extends ViewportHit<C> {

  final Point2D offset;

  HitAfterCells(Point2D offset) {
    this.offset = offset;
  }

  @Override
  public boolean isCellHit() {
    return false;
  }

  @Override
  public boolean isBeforeCells() {
    return false;
  }

  @Override
  public boolean isAfterCells() {
    return true;
  }

  @Override
  public int getCellIndex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public C getCell() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point2D getCellOffset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point2D getOffsetBeforeCells() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point2D getOffsetAfterCells() {
    return offset;
  }

}