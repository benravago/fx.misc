package fx.react;

import java.util.function.Predicate;

class FilterStream<T> extends EventStreamBase<T> {

  final EventStream<T> source;
  final Predicate<? super T> predicate;

  FilterStream(EventStream<T> source, Predicate<? super T> predicate) {
    this.source = source;
    this.predicate = predicate;
  }

  @Override
  protected Subscription observeInputs() {
    return source.subscribe(t -> {
      if (predicate.test(t)) {
        emit(t);
      }
    });
  }

}
