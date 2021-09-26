package fx.state;

import fx.state.machine.InitialState;

public interface StateMachine {
  static <S> InitialState<S> init(S initialState) {
    return InitialState.of(initialState);
  }
}