package fx.react.util;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

class NonAccumulativeStreamNotifications<T> extends NotificationAccumulatorBase<Consumer<? super T>, T, T> implements NoAccumulation<T> {

  @Override
  protected AccumulatorSize size(Consumer<? super T> observer, T accumulatedValue) {
    return AccumulatorSize.ONE;
  }

  @Override
  protected Runnable head(Consumer<? super T> observer, T accumulatedValue) {
    return () -> observer.accept(accumulatedValue);
  }

  @Override
  protected T tail(Consumer<? super T> observer, T accumulatedValue) {
    throw new NoSuchElementException();
  }

}