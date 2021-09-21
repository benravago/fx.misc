package fx.react;

import java.util.Deque;

import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

/**
 * See {@link EventStream#pausable()}
 */
class PausableEventStream<T> extends SuspendableEventStreamBase<T, Deque<T>> {

  PausableEventStream(EventStream<T> source) {
    super(source, NotificationAccumulator.queuingStreamNotifications());
  }

  @Override
  protected AccumulatorSize sizeOf(Deque<T> accum) {
    return AccumulatorSize.fromInt(accum.size());
  }

  @Override
  protected T headOf(Deque<T> accum) {
    return accum.getFirst();
  }

  @Override
  protected Deque<T> tailOf(Deque<T> accum) {
    accum.removeFirst();
    return accum;
  }

}