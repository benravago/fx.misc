package fx.react;

import java.util.Objects;

/**
 * See {@link EventStream#distinct()}
 */
class DistinctStream<T> extends EventStreamBase<T> {

  static final Object NONE = new Object();
  final EventStream<T> input;
  Object previous = NONE;

  DistinctStream(EventStream<T> input) {
    this.input = input;
  }

  @Override
  protected Subscription observeInputs() {
    return input.subscribe(value -> {
      var prevToCompare = previous;
      previous = value;
      if (!Objects.equals(value, prevToCompare)) {
        emit(value);
      }
    });
  }

}