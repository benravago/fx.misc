package fx.react.util;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

class AccumulativeStreamNotifications<T, A> extends NotificationAccumulatorBase<Consumer<? super T>, T, A> {

  final Function<? super A, AccumulatorSize> size;
  final Function<? super A, ? extends T> head;
  final Function<? super A, ? extends A> tail;
  final Function<? super T, ? extends A> initialTransformation;
  final BiFunction<? super A, ? super T, ? extends A> reduction;

  AccumulativeStreamNotifications(Function<? super A, AccumulatorSize> size, Function<? super A, ? extends T> head, Function<? super A, ? extends A> tail, Function<? super T, ? extends A> initialTransformation, BiFunction<? super A, ? super T, ? extends A> reduction) {
    this.size = size;
    this.head = head;
    this.tail = tail;
    this.initialTransformation = initialTransformation;
    this.reduction = reduction;
  }

  @Override
  protected AccumulatorSize size(Consumer<? super T> observer, A accumulatedValue) {
    return size.apply(accumulatedValue);
  }

  @Override
  protected Runnable head(Consumer<? super T> observer, A accumulatedValue) {
    var event = head.apply(accumulatedValue);
    return () -> observer.accept(event);
  }

  @Override
  protected A tail(Consumer<? super T> observer, A accumulatedValue) {
    return tail.apply(accumulatedValue);
  }

  @Override
  public A initialAccumulator(T value) {
    return initialTransformation.apply(value);
  }

  @Override
  public A reduce(A accum, T value) {
    return reduction.apply(accum, value);
  }

}
