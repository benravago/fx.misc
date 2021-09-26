package fx.state.machine;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import fx.react.EventStream;

class InputHandlerBuilder<S, TGT> {

  final Function<Consumer<Function<S, TGT>>, InputHandler> inputSubscriberProvider;

  <I> InputHandlerBuilder(EventStream<I> input, BiFunction<? super S, ? super I, ? extends TGT> f) {
    this.inputSubscriberProvider = publisher -> {
      return () -> input.subscribe(i -> publisher.accept(s -> f.apply(s, i)));
    };
  }

  public InputHandler build(Consumer<Function<S, TGT>> c) {
    return inputSubscriberProvider.apply(c);
  }

}