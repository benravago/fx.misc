package fx.react;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

abstract class ReducibleEventStreamBase<T> extends SuspendableEventStreamBase<T, T> {

  protected ReducibleEventStreamBase(EventStream<T> source, NotificationAccumulator<Consumer<? super T>, T, T> pn) {
    super(source, pn);
  }

  @Override
  protected final AccumulatorSize sizeOf(T accum) {
    return AccumulatorSize.ONE;
  }

  @Override
  protected final T headOf(T accum) {
    return accum;
  }

  @Override
  protected final T tailOf(T accum) {
    throw new NoSuchElementException();
  }

}