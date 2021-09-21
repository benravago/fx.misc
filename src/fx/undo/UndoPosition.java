package fx.undo;

/**
 * Represents a position in UndoManager's history.
 */
public interface UndoPosition {

  /**
   * Sets the mark of the underlying UndoManager at this position.
   * The mark is set whether or not this position is valid. It is
   * OK for an UndoManager to be marked at an invalid position.
   */
  void mark();

  /**
   * Checks whether this history position is still valid.
   * A position becomes invalid when
   * <ul>
   *   <li>the change immediately preceding the position is undone
   *   and then discarded due to another incoming change; or</li>
   *   <li>the change immediately following the position is forgotten
   *   due to history size limit.</li>
   * </ul>
   */
  boolean isValid();

}