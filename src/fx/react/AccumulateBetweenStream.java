package fx.react;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

class AccumulateBetweenStream<T, A> extends EventStreamBase<T> {

  final EventStream<T> source;
  final EventStream<?> ticks;
  final Function<? super T, ? extends A> initialTransformation;
  final BiFunction<? super A, ? super T, ? extends A> accumulation;
  final Function<? super A, List<T>> deconstruction;

  boolean hasValue = false;
  A accum = null;

  AccumulateBetweenStream(EventStream<T> source, EventStream<?> ticks, Function<? super T, ? extends A> initialTransformation, BiFunction<? super A, ? super T, ? extends A> accumulation, Function<? super A, List<T>> deconstruction) {
    this.source = source;
    this.ticks = ticks;
    this.initialTransformation = initialTransformation;
    this.accumulation = accumulation;
    this.deconstruction = deconstruction;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.subscribe(this::handleEvent);
    var s2 = ticks.subscribe(this::handleTick);
    return s1.and(s2).and(this::reset);
  }

  void handleEvent(T event) {
    if (hasValue) {
      accum = accumulation.apply(accum, event);
    } else {
      accum = initialTransformation.apply(event);
      hasValue = true;
    }
  }

  void handleTick(Object tick) {
    if (hasValue) {
      var toEmit = deconstruction.apply(accum);
      reset();
      for (var t : toEmit) {
        emit(t);
      }
    }
  }

  void reset() {
    hasValue = false;
    accum = null;
  }

}
