package fx.state.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import fx.react.EventStreamBase;
import fx.react.Subscription;
import fx.util.Sequence;

class StatefulStream<S, O> extends EventStreamBase<O> {

  final List<InputHandler> inputHandlers;

  S state;

  StatefulStream(S initialState, Sequence<TransitionBuilder<S>> transitions, Sequence<EmissionBuilder<S, O>> emissions, Sequence<TransmissionBuilder<S, O>> transmissions) {

    state = initialState;

    this.inputHandlers = new ArrayList<>(transitions.size() + emissions.size() + transmissions.size());

    for (var tb : transitions) {
      inputHandlers.add(tb.build(this::handleTransition));
    }
    for (var eb : emissions) {
      inputHandlers.add(eb.build(this::handleEmission));
    }
    for (var tb : transmissions) {
      inputHandlers.add(tb.build(this::handleTransmission));
    }
  }

  @Override
  protected Subscription observeInputs() {
    return Subscription.multi(InputHandler::subscribeToInput, inputHandlers);
  }

  private void handleTransition(Function<S, S> transition) {
    state = transition.apply(state);
  }

  private void handleEmission(Function<S, Optional<O>> emission) {
    emission.apply(state).ifPresent(this::emit);
  }

  void handleTransmission(Function<S, Transmission<S, Optional<O>>> transmission) {
    var pair = transmission.apply(state);
    state = pair.state();
    pair.emission().ifPresent(this::emit);
  }

}
