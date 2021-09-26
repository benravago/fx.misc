package fx.state.machine;

import java.util.function.BiFunction;

import fx.react.EventStream;

class TransitionBuilder<S> extends InputHandlerBuilder<S, S> {

  <I> TransitionBuilder(EventStream<I> input, BiFunction<? super S, ? super I, ? extends S> f) {
    super(input, f);
  }

}