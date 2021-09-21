package fx.react;

import java.util.function.BiFunction;
import java.util.function.Function;

import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

class AccumulativeEventStream<T, A> extends SuspendableEventStreamBase<T, A> {

  final Function<? super A, AccumulatorSize> size;
  final Function<? super A, ? extends T> head;
  final Function<? super A, ? extends A> tail;

  AccumulativeEventStream(EventStream<T> source, Function<? super T, ? extends A> initialTransformation, BiFunction<? super A, ? super T, ? extends A> accumulation, Function<? super A, AccumulatorSize> size, Function<? super A, ? extends T> head, Function<? super A, ? extends A> tail) {
    super(source, NotificationAccumulator.accumulativeStreamNotifications(size, head, tail, initialTransformation, accumulation));
    this.size = size;
    this.head = head;
    this.tail = tail;
  }

  @Override
  protected AccumulatorSize sizeOf(A accum) {
    return size.apply(accum);
  }

  @Override
  protected T headOf(A accum) {
    return head.apply(accum);
  }

  @Override
  protected A tailOf(A accum) {
    return tail.apply(accum);
  }

}
