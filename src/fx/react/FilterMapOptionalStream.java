package fx.react;

import java.util.Optional;
import java.util.function.Function;

class FilterMapOptionalStream<T, U> extends EventStreamBase<U> {

  final EventStream<T> source;
  final Function<? super T, Optional<U>> mapper;

  FilterMapOptionalStream(EventStream<T> src, Function<? super T, Optional<U>> f) {
    this.source = src;
    this.mapper = f;
  }

  @Override
  protected Subscription observeInputs() {
    return source.subscribe(t -> mapper.apply(t).ifPresent(this::emit));
  }

}
