package fx.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Immutable singly-linked list.
 */
public abstract class Sequence<T> implements Iterable<T> {

  public static <T> Sequence<T> nil() {
    return VoidSequence.instance();
  }

  public static <T> SubSequence<T> cons(T head, Sequence<? extends T> tail) {
    return new SubSequence<>(head, tail);
  }

  @SafeVarargs
  public static <T> SubSequence<T> of(T head, T... tail) {
    return cons(head, of(tail, tail.length, Sequence.<T>nil()));
  }

  private static <T> Sequence<T> of(T[] elems, int to, Sequence<T> tail) {
    return (to == 0) ? tail : of(elems, to - 1, cons(elems[to - 1], tail));
  }

  public static <T> Sequence<? extends T> concat(Sequence<? extends T> l1, Sequence<? extends T> l2) {
    return (l1.isEmpty()) ? l2 : cons(l1.head(), concat(l1.tail(), l2));
  }

  public abstract boolean isEmpty();

  public abstract int size();

  public abstract T head();

  public abstract Sequence<? extends T> tail();

  public abstract <U> Sequence<U> map(Function<? super T, ? extends U> f);

  public abstract <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction);

  public abstract <R> Optional<R> mapReduce(Function<? super T, ? extends R> map, BinaryOperator<R> reduce);

  public boolean all(Predicate<T> cond) {
    return fold(true, (b, t) -> b && cond.test(t));
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "[]";
    } else {
      var sb = new StringBuilder();
      sb.append("[");
      sb.append(head());
      var tail = tail();
      while (!tail.isEmpty()) {
        sb.append(",").append(tail.head());
        tail = tail.tail();
      }
      sb.append("]");
      return sb.toString();
    }
  }

  public Stream<T> stream() {
    var spliterator = new Spliterator<T>() {
      final Iterator<T> iterator = iterator();

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        if (iterator.hasNext()) {
          action.accept(iterator.next());
          return true;
        } else {
          return false;
        }
      }
      @Override
      public Spliterator<T> trySplit() {
        return null;
      }
      @Override
      public long estimateSize() {
        return size();
      }
      @Override
      public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED;
      }
    };

    return StreamSupport.stream(spliterator, false);
  }

}
