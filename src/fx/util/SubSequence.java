package fx.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class SubSequence<T> extends Sequence<T> {

  final T head;
  final Sequence<? extends T> tail;
  final int size;

  SubSequence(T head, Sequence<? extends T> tail) {
    this.head = head;
    this.tail = tail;
    this.size = 1 + tail.size();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public T head() {
    return head;
  }

  @Override
  public Sequence<? extends T> tail() {
    return tail;
  }

  @Override
  public <U> SubSequence<U> map(Function<? super T, ? extends U> f) {
    return cons(f.apply(head), tail.map(f));
  }

  @Override
  public <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction) {
    return tail.fold(reduction.apply(acc, head), reduction);
  }

  @Override
  public final Iterator<T> iterator() {
    return new Iterator<T>() {
      Sequence<? extends T> l = SubSequence.this;
      @Override
      public boolean hasNext() {
        return !l.isEmpty();
      }
      @Override
      public T next() {
        var res = l.head();
        l = l.tail();
        return res;
      }
    };
  }

  @Override
  public <R> Optional<R> mapReduce(Function<? super T, ? extends R> map, BinaryOperator<R> reduce) {
    return Optional.of(mapReduce1(map, reduce));
  }

  public <R> R mapReduce1(Function<? super T, ? extends R> map, BinaryOperator<R> reduce) {
    var acc = map.apply(head);
    return tail.fold(acc, (r, t) -> reduce.apply(r, map.apply(t)));
  }

}