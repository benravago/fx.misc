package fx.state.machine;

import fx.react.EventStream;
import fx.util.Sequence;

class StatefulStreamBuilderBase<S, O> implements StatefulStreamBuilder<S, O> {

  final S initialState;
  final Sequence<TransitionBuilder<S>> transitions;
  final Sequence<EmissionBuilder<S, O>> emissions;
  final Sequence<TransmissionBuilder<S, O>> transmissions;

  StatefulStreamBuilderBase(S initialState, Sequence<TransitionBuilder<S>> transitions, Sequence<EmissionBuilder<S, O>> emissions, Sequence<TransmissionBuilder<S, O>> transmissions) {
    this.initialState = initialState;
    this.transitions = transitions;
    this.emissions = emissions;
    this.transmissions = transmissions;
  }

  @Override
  public <I> StatefulStreamBuilderOn<S, O, I> on(EventStream<I> input) {
    return new StatefulStreamBuilderOnBase<>(initialState, transitions, emissions, transmissions, input);
  }

  @Override
  public EventStream<O> toEventStream() {
    return new StatefulStream<>(initialState, transitions, emissions, transmissions);
  }

}