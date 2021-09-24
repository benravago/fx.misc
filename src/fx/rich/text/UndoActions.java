package fx.rich.text;

import fx.undo.UndoManager;
import fx.react.value.Val;

/**
 * Undo/redo actions for {@link TextEditingArea}.
 */
public interface UndoActions {

  /**
   * Undo manager of this text area.
   */
  UndoManager<?> getUndoManager();

  /**
   * Closes the current area's undo manager before setting it to the given one. <b>Note:</b> to create your
   * own {@link UndoManager}, see the convenient factory methods in {@link fx.rich.text.util.UndoUtils}.
   */
  void setUndoManager(UndoManager<?> undoManager);

  default void undo() {
    getUndoManager().undo();
  }

  default void redo() {
    getUndoManager().redo();
  }

  default boolean isUndoAvailable() {
    return getUndoManager().isUndoAvailable();
  }

  default Val<Boolean> undoAvailableProperty() {
    return getUndoManager().undoAvailableProperty();
  }

  default boolean isRedoAvailable() {
    return getUndoManager().isRedoAvailable();
  }

  default Val<Boolean> redoAvailableProperty() {
    return getUndoManager().redoAvailableProperty();
  }

}
