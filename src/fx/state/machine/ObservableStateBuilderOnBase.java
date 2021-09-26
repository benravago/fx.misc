package fx.state.machine;

import static fx.util.Sequence.cons;
import static fx.util.Sequence.nil;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.react.EventStream;
import fx.util.Sequence;

class ObservableStateBuilderOnBase<S, I> implements ObservableStateBuilderOn<S, I> {

  final S initialState;
  final Sequence<TransitionBuilder<S>> transitions;
  final EventStream<I> input;

  ObservableStateBuilderOnBase(S initialState, Sequence<TransitionBuilder<S>> transitions, EventStream<I> input) {
    this.initialState = initialState;
    this.transitions = transitions;
    this.input = input;
  }

  @Override
  public ObservableStateBuilder<S> transition(BiFunction<? super S, ? super I, ? extends S> f) {
    var transition = new TransitionBuilder<S>(input, f);
    return new ObservableStateBuilderBase<>(initialState, cons(transition, transitions));
  }

  @Override
  public <O> StatefulStreamBuilder<S, O> emit(BiFunction<? super S, ? super I, Optional<O>> f) {
    var emission = new EmissionBuilder<S, O>(input, f);
    return new StatefulStreamBuilderBase<>(initialState, transitions, cons(emission, nil()), nil());
  }

  @Override
  public <O> StatefulStreamBuilder<S, O> transmit(BiFunction<? super S, ? super I, Transmission<S, Optional<O>>> f) {
    var transmission = new TransmissionBuilder<S, O>(input, f);
    return new StatefulStreamBuilderBase<>(initialState, transitions, nil(), cons(transmission, nil()));
  }

}
