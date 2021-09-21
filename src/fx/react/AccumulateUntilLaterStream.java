package fx.react;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

class AccumulateUntilLaterStream<T, A> extends EventStreamBase<T> {

  final EventStream<T> source;
  final Function<? super T, ? extends A> initialTransformation;
  final BiFunction<? super A, ? super T, ? extends A> accumulation;
  final Function<? super A, List<T>> deconstruction;
  final Executor eventThreadExecutor;

  boolean hasValue = false;
  A accum = null;

  AccumulateUntilLaterStream(EventStream<T> source, Function<? super T, ? extends A> initialTransformation, BiFunction<? super A, ? super T, ? extends A> accumulation, Function<? super A, List<T>> deconstruction, Executor eventThreadExecutor) {
    this.source = source;
    this.initialTransformation = initialTransformation;
    this.accumulation = accumulation;
    this.deconstruction = deconstruction;
    this.eventThreadExecutor = eventThreadExecutor;
  }

  @Override
  protected Subscription observeInputs() {
    return source.subscribe(this::handleEvent);
  }

  void handleEvent(T event) {
    if (hasValue) {
      accum = accumulation.apply(accum, event);
      // emission already scheduled
    } else {
      accum = initialTransformation.apply(event);
      hasValue = true;
      eventThreadExecutor.execute(this::emitAccum);
    }
  }

  void emitAccum() {
    assert hasValue;
    hasValue = false;
    var toEmit = deconstruction.apply(accum);
    accum = null;
    for (var t : toEmit) {
      emit(t);
    }
  }

}
