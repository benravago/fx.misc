package fx.state.machine;

import static fx.util.Sequence.nil;

import fx.react.EventStream;

class InitialStateBase<S> implements InitialState<S> {

  final S initialState;

  InitialStateBase(S initialState) {
    this.initialState = initialState;
  }

  @Override
  public <I> ObservableStateBuilderOn<S, I> on(EventStream<I> input) {
    return new ObservableStateBuilderOnBase<>(initialState, nil(), input);
  }

}