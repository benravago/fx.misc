package fx.layout.flow;

/**
 * A {@link TargetPosition} that instructs its {@link TargetPositionVisitor} to use the cell at {@link #itemIndex}
 * as the anchor cell, showing it at the "bottom" of the viewport and to offset it by {@link #offsetFromEnd}.
 */
class EndOffEnd implements TargetPosition {

  final int itemIndex;
  final double offsetFromEnd;

  EndOffEnd(int itemIndex, double offsetFromEnd) {
    this.itemIndex = itemIndex;
    this.offsetFromEnd = offsetFromEnd;
  }

  @Override
  public TargetPosition transformByChange(int pos, int removedSize, int addedSize) {
    if (itemIndex >= pos + removedSize) {
      // change before the target item, just update item index
      return new EndOffEnd(itemIndex - removedSize + addedSize, offsetFromEnd);
    } else if (itemIndex >= pos) {
      // target item deleted
      if (addedSize == removedSize) {
        return this;
      } else {
        // show the last inserted at the target offset
        return new EndOffEnd(pos + addedSize - 1, offsetFromEnd);
      }
    } else {
      // change after the target item, target position not affected
      return this;
    }
  }

  @Override
  public TargetPosition scrollBy(double delta) {
    return new EndOffEnd(itemIndex, offsetFromEnd - delta);
  }

  @Override
  public void accept(TargetPositionVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public TargetPosition clamp(int size) {
    return new EndOffEnd(StartOffStart.clamp(itemIndex, size), offsetFromEnd);
  }

}