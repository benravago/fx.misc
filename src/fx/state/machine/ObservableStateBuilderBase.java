package fx.state.machine;

import fx.react.EventStream;
import fx.util.Sequence;
import javafx.beans.binding.Binding;

class ObservableStateBuilderBase<S> implements ObservableStateBuilder<S> {

  final S initialState;
  final Sequence<TransitionBuilder<S>> transitions;

  ObservableStateBuilderBase(S initialState, Sequence<TransitionBuilder<S>> transitions) {
    this.initialState = initialState;
    this.transitions = transitions;
  }

  @Override
  public <I> ObservableStateBuilderOn<S, I> on(EventStream<I> input) {
    return new ObservableStateBuilderOnBase<>(initialState, transitions, input);
  }

  @Override
  public EventStream<S> toStateStream() {
    return new StateStream<>(initialState, transitions);
  }

  @Override
  public Binding<S> toObservableState() {
    return toStateStream().toBinding(initialState);
  }

}