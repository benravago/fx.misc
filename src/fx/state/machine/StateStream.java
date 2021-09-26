package fx.state.machine;

import java.util.function.Function;

import fx.react.EventStreamBase;
import fx.react.Subscription;
import fx.util.Sequence;

class StateStream<S> extends EventStreamBase<S> {

  final InputHandler[] inputHandlers;

  S state;

  StateStream(S initialState, Sequence<TransitionBuilder<S>> transitions) {
    inputHandlers = transitions.stream().map(t -> t.build(this::handleTransition)).toArray(n -> new InputHandler[n]);
    state = initialState;
  }

  @Override
  protected Subscription observeInputs() {
    return Subscription.multi(InputHandler::subscribeToInput, inputHandlers);
  }

  private void handleTransition(Function<S, S> transition) {
    state = transition.apply(state);
    emit(state);
  }

}