package fx.react;

import java.util.function.Function;

/**
 * See {@link EventStream#map(Function)}
 */
class MappedStream<T, U> extends EventStreamBase<U> {

  final EventStream<T> input;
  final Function<? super T, ? extends U> f;

  MappedStream(EventStream<T> input, Function<? super T, ? extends U> f) {
    this.input = input;
    this.f = f;
  }

  @Override
  protected Subscription observeInputs() {
    return input.subscribe(value -> {
      emit(f.apply(value));
    });
  }

}
