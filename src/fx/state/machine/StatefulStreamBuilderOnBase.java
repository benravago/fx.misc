package fx.state.machine;

import static fx.util.Sequence.cons;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.react.EventStream;
import fx.util.Sequence;

class StatefulStreamBuilderOnBase<S, O, I> implements StatefulStreamBuilderOn<S, O, I> {

  final S initialState;
  final Sequence<TransitionBuilder<S>> transitions;
  final Sequence<EmissionBuilder<S, O>> emissions;
  final Sequence<TransmissionBuilder<S, O>> transmissions;
  final EventStream<I> input;

  StatefulStreamBuilderOnBase(S initialState, Sequence<TransitionBuilder<S>> transitions, Sequence<EmissionBuilder<S, O>> emissions, Sequence<TransmissionBuilder<S, O>> transmissions, EventStream<I> input) {
    this.initialState = initialState;
    this.transitions = transitions;
    this.emissions = emissions;
    this.transmissions = transmissions;
    this.input = input;
  }

  @Override
  public StatefulStreamBuilder<S, O> transition(BiFunction<? super S, ? super I, ? extends S> f) {
    var transition = new TransitionBuilder<S>(input, f);
    return new StatefulStreamBuilderBase<>(initialState, cons(transition, transitions), emissions, transmissions);
  }

  @Override
  public StatefulStreamBuilder<S, O> emit(BiFunction<? super S, ? super I, Optional<O>> f) {
    var emission = new EmissionBuilder<S, O>(input, f);
    return new StatefulStreamBuilderBase<>(initialState, transitions, cons(emission, emissions), transmissions);
  }

  @Override
  public StatefulStreamBuilder<S, O> transmit(BiFunction<? super S, ? super I, Transmission<S, Optional<O>>> f) {
    var transmission = new TransmissionBuilder<S, O>(input, f);
    return new StatefulStreamBuilderBase<>(initialState, transitions, emissions, cons(transmission, transmissions));
  }

}
