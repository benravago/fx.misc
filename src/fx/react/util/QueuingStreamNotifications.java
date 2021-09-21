package fx.react.util;

import java.util.Deque;
import java.util.function.Consumer;

class QueuingStreamNotifications<T> extends NotificationAccumulatorBase<Consumer<? super T>, T, Deque<T>> implements QueuingAccumulation<T> {

  @Override
  protected AccumulatorSize size(Consumer<? super T> observer, Deque<T> accumulatedValue) {
    return AccumulatorSize.fromInt(accumulatedValue.size());
  }

  @Override
  protected Runnable head(Consumer<? super T> observer, Deque<T> accumulatedValue) {
    var t = accumulatedValue.getFirst();
    return () -> observer.accept(t);
  }

  @Override
  protected Deque<T> tail(Consumer<? super T> observer, Deque<T> accumulatedValue) {
    accumulatedValue.removeFirst();
    return accumulatedValue;
  }

}