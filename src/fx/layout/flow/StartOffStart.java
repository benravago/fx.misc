package fx.layout.flow;

/**
 * A {@link TargetPosition} that instructs its {@link TargetPositionVisitor} to use the cell at {@link #itemIndex}
 * as the anchor cell, showing it at the "top" of the viewport and to offset it by {@link #offsetFromStart}.
 */
class StartOffStart implements TargetPosition {

  final int itemIndex;
  final double offsetFromStart;

  StartOffStart(int itemIndex, double offsetFromStart) {
    this.itemIndex = itemIndex;
    this.offsetFromStart = offsetFromStart;
  }

  @Override
  public TargetPosition transformByChange(int pos, int removedSize, int addedSize) {
    if (itemIndex >= pos + removedSize) {
      // change before the target item, just update item index
      return new StartOffStart(itemIndex - removedSize + addedSize, offsetFromStart);
    } else if (itemIndex >= pos) {
      // target item deleted
      if (addedSize == removedSize) {
        return this;
      } else {
        // show the first inserted at the target offset
        return new StartOffStart(pos, offsetFromStart);
      }
    } else {
      // change after the target item, target position not affected
      return this;
    }
  }

  @Override
  public TargetPosition scrollBy(double delta) {
    return new StartOffStart(itemIndex, offsetFromStart - delta);
  }

  @Override
  public void accept(TargetPositionVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public TargetPosition clamp(int size) {
    return new StartOffStart(clamp(itemIndex, size), offsetFromStart);
  }

  static int clamp(int idx, int size) {
    if (size < 0) {
      throw new IllegalArgumentException("size cannot be negative: " + size);
    }
    if (idx <= 0) {
      return 0;
    } else if (idx >= size) {
      return size - 1;
    } else {
      return idx;
    }
  }

}