package fx.react.collection;

/**
 * Represents a change of a value.
 * @param <T> type of the value that changed.
 */
public class Change<T> {

  final T oldValue;
  final T newValue;

  public Change(T oldValue, T newValue) {
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public T getOldValue() {
    return oldValue;
  }

  public T getNewValue() {
    return newValue;
  }

}
