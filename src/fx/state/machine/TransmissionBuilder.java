package fx.state.machine;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.react.EventStream;

class TransmissionBuilder<S, O> extends InputHandlerBuilder<S, Transmission<S, Optional<O>>> {

  <I> TransmissionBuilder(EventStream<I> input, BiFunction<? super S, ? super I, ? extends Transmission<S, Optional<O>>> f) {
    super(input, f);
  }

}
