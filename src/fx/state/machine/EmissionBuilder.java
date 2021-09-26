package fx.state.machine;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.react.EventStream;

class EmissionBuilder<S, O> extends InputHandlerBuilder<S, Optional<O>> {

  <I> EmissionBuilder(EventStream<I> input, BiFunction<? super S, ? super I, ? extends Optional<O>> f) {
    super(input, f);
  }

}