package fx.react.util;

import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import fx.react.collection.ListModificationSequence;
import fx.react.collection.Observer;
import fx.react.collection.QuasiChange;

/**
 * @param <O> observer type
 * @param <V> type of produced values
 * @param <A> type of accumulated value
 */
public interface NotificationAccumulator<O, V, A> {

  static <T, A> NotificationAccumulator<Consumer<? super T>, T, A> accumulativeStreamNotifications(Function<? super A, AccumulatorSize> size, Function<? super A, ? extends T> head, Function<? super A, ? extends A> tail, Function<? super T, ? extends A> initialTransformation, BiFunction<? super A, ? super T, ? extends A> reduction) {
    return new AccumulativeStreamNotifications<>(size, head, tail, initialTransformation, reduction);
  }

  static <T> NotificationAccumulator<Consumer<? super T>, T, Deque<T>> queuingStreamNotifications() {
    return new QueuingStreamNotifications<T>();
  }

  static <T> NotificationAccumulator<Consumer<? super T>, T, T> reducingStreamNotifications(BinaryOperator<T> reduction) {
    return new ReducingStreamNotifications<>(reduction);
  }

  static <T> NotificationAccumulator<Consumer<? super T>, T, T> retainLatestStreamNotifications() {
    return new RetainLatestStreamNotifications<T>();
  }

  static <T> NotificationAccumulator<Consumer<? super T>, T, T> retainOldestValNotifications() {
    return new RetainOldestStreamNotifications<T>();
  }

  static <T> NotificationAccumulator<Consumer<? super T>, T, T> nonAccumulativeStreamNotifications() {
    return new NonAccumulativeStreamNotifications<T>();
  }

  static <E> NotificationAccumulator<Observer<? super E, ?>, QuasiChange<? extends E>, ListModificationSequence<E>> listNotifications() {
    return new ListNotifications<E>();
  }

  boolean isEmpty();

  Runnable takeOne();

  void addAll(Iterator<O> observers, V value);

  void clear();

  AccumulationFacility<V, A> getAccumulationFacility();

}
