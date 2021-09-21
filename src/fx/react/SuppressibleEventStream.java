package fx.react;

import java.util.NoSuchElementException;

import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

class SuppressibleEventStream<T> extends SuspendableEventStreamBase<T, T> {

  SuppressibleEventStream(EventStream<T> source) {
    super(source, NotificationAccumulator.nonAccumulativeStreamNotifications());
  }

  @Override
  protected AccumulatorSize sizeOf(T accum) {
    return AccumulatorSize.ZERO;
  }

  @Override
  protected T headOf(T accum) {
    throw new NoSuchElementException();
  }

  @Override
  protected T tailOf(T accum) {
    throw new NoSuchElementException();
  }

  @Override
  protected T initialAccumulator(T value) {
    return null;
  }

  // Override reduce so that it permits accumulation.
  @Override
  protected T reduce(T accum, T value) {
    return null;
  }

}