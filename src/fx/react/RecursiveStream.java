package fx.react;

import java.util.function.Consumer;

import fx.react.util.NotificationAccumulator;

class RecursiveStream<T> extends EventStreamBase<T> {

  final EventStream<T> input;

  RecursiveStream(EventStream<T> input, NotificationAccumulator<Consumer<? super T>, T, ?> pn) {
    super(pn);
    this.input = input;
  }

  @Override
  protected Subscription observeInputs() {
    return input.subscribe(this::emit);
  }

}