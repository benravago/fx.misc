package fx.layout.flow;

/**
 * Determines how the cells in the viewport should be laid out and where any extra unused space should exist
 * if there are not enough cells to completely fill up the viewport
 */
public enum Gravity {

  /**
   * If using a {@link VerticalHelper vertical viewport}, lays out the content from top-to-bottom. The first
   * visible item will appear at the top and the last visible item (or unused space) towards the bottom.
   * <p>
   * If using a {@link HorizontalHelper horizontal viewport}, lays out the content from left-to-right. The first
   * visible item will appear at the left and the last visible item (or unused space) towards the right.
   * </p>
   */
  FRONT,
  /**
   * If using a {@link VerticalHelper vertical viewport}, lays out the content from bottom-to-top. The first
   * visible item will appear at the bottom and the last visible item (or unused space) towards the top.
   * <p>
   * If using a {@link HorizontalHelper horizontal viewport}, lays out the content from right-to-left. The first
   * visible item will appear at the right and the last visible item (or unused space) towards the left.
   * </p>
   */
  REAR

}