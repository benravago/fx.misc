package fx.state.machine;

import fx.react.EventStream;
import javafx.beans.binding.Binding;

public interface ObservableStateBuilder<S> {

  <I> ObservableStateBuilderOn<S, I> on(EventStream<I> input);

  /**
   * Returns an event stream that emits the current state of the state
   * machine every time it changes.
   */
  EventStream<S> toStateStream();

  /**
   * Returns a binding that reflects the current state of the state
   * machine. Disposing the returned binding (by calling its
   * {@code dispose()} method) causes the state machine to unsubscribe
   * from the event streams that alter its state and allows the state
   * machine to be garbage collected.
   */
  Binding<S> toObservableState();

}