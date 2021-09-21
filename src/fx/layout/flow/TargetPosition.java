package fx.layout.flow;

/**
 * Defines where the {@link Navigator} should place the anchor cell's node in the viewport. Its three implementations
 * are {@link StartOffStart}, {@link EndOffEnd}, and {@link MinDistanceTo}.
 */
interface TargetPosition {

  static TargetPosition BEGINNING = new StartOffStart(0, 0.0);

  /**
   * When the list of items, those displayed in the viewport, and those that are not, are modified, transforms
   * this change to account for those modifications.
   *
   * @param pos the cell index where the change begins
   * @param removedSize the amount of cells that were removed, starting from {@code pos}
   * @param addedSize the amount of cells that were added, starting from {@code pos}
   */
  TargetPosition transformByChange(int pos, int removedSize, int addedSize);

  TargetPosition scrollBy(double delta);

  /**
   * Visitor Pattern: prevents type-checking the implementation
   */
  void accept(TargetPositionVisitor visitor);

  /**
   * Insures this position's item index is between 0 and {@code size}
   */
  TargetPosition clamp(int size);

}