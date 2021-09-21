package fx.layout.flow;

class MinDistanceTo implements TargetPosition {

  final int itemIndex;
  final Offset minY;
  final Offset maxY;

  MinDistanceTo(int itemIndex, Offset minY, Offset maxY) {
    this.itemIndex = itemIndex;
    this.minY = minY;
    this.maxY = maxY;
  }

  public MinDistanceTo(int itemIndex) {
    this(itemIndex, Offset.fromStart(0.0), Offset.fromEnd(0.0));
  }

  @Override
  public TargetPosition transformByChange(int pos, int removedSize, int addedSize) {
    if (itemIndex >= pos + removedSize) {
      // change before the target item, just update item index
      return new MinDistanceTo(itemIndex - removedSize + addedSize, minY, maxY);
    } else if (itemIndex >= pos) {
      // target item deleted
      if (addedSize == removedSize) {
        return this;
      } else {
        // show the first inserted
        return new MinDistanceTo(pos, Offset.fromStart(0.0), Offset.fromEnd(0.0));
      }
    } else {
      // change after the target item, target position not affected
      return this;
    }
  }

  @Override
  public TargetPosition scrollBy(double delta) {
    return new MinDistanceTo(itemIndex, minY.add(delta), maxY.add(delta));
  }

  @Override
  public void accept(TargetPositionVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public TargetPosition clamp(int size) {
    return new MinDistanceTo(StartOffStart.clamp(itemIndex, size), minY, maxY);
  }

}