package fx.react;

import javafx.beans.value.ObservableValue;

/**
 * An event stream whose emission of events can be suspended temporarily. What
 * events, if any, are emitted when emission is resumed depends on the concrete
 * implementation.
 */
public interface SuspendableEventStream<T> extends EventStream<T>, Suspendable {

  /**
   * Returns an event stream that is suspended when the given
   * {@code condition} is {@code true} and emits normally when
   * {@code condition} is {@code false}.
   */
  default EventStream<T> suspendedWhen(ObservableValue<Boolean> condition) {
    return new SuspendedWhenStream<>(this, condition);
  }

}
