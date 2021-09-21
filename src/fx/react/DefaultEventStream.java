package fx.react;

import java.util.function.Consumer;

class DefaultEventStream<T> extends EventStreamBase<T> {

  final EventStream<T> input;
  final T initial;

  T latestEvent = null;
  boolean firstObserver = true;
  boolean emitted = false;

  DefaultEventStream(EventStream<T> input, T initial) {
    this.input = input;
    this.initial = initial;
  }

  @Override
  protected void newObserver(Consumer<? super T> observer) {
    if (firstObserver) {
      firstObserver = false;
      if (!emitted) {
        observer.accept(initial);
      }
    } else {
      observer.accept(latestEvent);
    }
  }

  @Override
  protected final Subscription observeInputs() {
    firstObserver = true;
    emitted = false;
    latestEvent = initial;
    return input.subscribe(x -> {
      latestEvent = x;
      emitted = true;
      emit(x);
    });
  }

}
