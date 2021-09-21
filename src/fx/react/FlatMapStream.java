package fx.react;

import java.util.function.Function;

/**
 * See {@link EventStream#flatMap(Function)}
 */
class FlatMapStream<T, U> extends EventStreamBase<U> {

  final EventStream<T> source;
  final Function<? super T, ? extends EventStream<U>> mapper;

  Subscription mappedSubscription = Subscription.EMPTY;

  public FlatMapStream(EventStream<T> src, Function<? super T, ? extends EventStream<U>> f) {
    this.source = src;
    this.mapper = f;
  }

  @Override
  protected Subscription observeInputs() {
    var s = source.subscribe(t -> {
      mappedSubscription.unsubscribe();
      mappedSubscription = mapper.apply(t).subscribe(this::emit);
    });
    return () -> {
      s.unsubscribe();
      mappedSubscription.unsubscribe();
      mappedSubscription = Subscription.EMPTY;
    };
  }

}
