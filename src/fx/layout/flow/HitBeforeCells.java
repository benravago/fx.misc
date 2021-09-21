package fx.layout.flow;

import javafx.geometry.Point2D;

class HitBeforeCells<C extends Cell<?, ?>> extends ViewportHit<C> {

  final Point2D offset;

  HitBeforeCells(Point2D offset) {
    this.offset = offset;
  }

  @Override
  public boolean isCellHit() {
    return false;
  }

  @Override
  public boolean isBeforeCells() {
    return true;
  }

  @Override
  public boolean isAfterCells() {
    return false;
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
    return offset;
  }

  @Override
  public Point2D getOffsetAfterCells() {
    throw new UnsupportedOperationException();
  }

}