package fx.state.machine;

import fx.react.EventStream;

public interface InitialState<S> {

  <I> ObservableStateBuilderOn<S, I> on(EventStream<I> input);

  static <S> InitialState<S> of(S initialState) {
    return new InitialStateBase<>(initialState);
  }

}