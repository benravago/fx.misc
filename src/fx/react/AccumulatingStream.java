package fx.react;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * See {@link EventStream#accumulate(BiFunction, Function)}
 */
class AccumulatingStream<T, U> extends EventStreamBase<U> {

  final EventStream<T> input;
  final Function<? super T, ? extends U> initialTransformation;
  final BiFunction<? super U, ? super T, ? extends U> reduction;

  boolean hasEvent = false;
  U event = null;

  AccumulatingStream(EventStream<T> input, Function<? super T, ? extends U> initial, BiFunction<? super U, ? super T, ? extends U> reduction) {
    this.input = input;
    this.initialTransformation = initial;
    this.reduction = reduction;
  }

  @Override
  protected final Subscription observeInputs() {
    return input.subscribe(i -> {
      event = hasEvent ? reduction.apply(event, i) : initialTransformation.apply(i);
      hasEvent = true;
      emit(event);
    });
  }

}
