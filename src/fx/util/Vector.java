package fx.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class Vector<T> {

  abstract T get(int index);
  abstract Vector<T> add(T elem);
  abstract Vector<T> remove(T elem);
  abstract void forEach(Consumer<? super T> f);
  abstract void forEachBetween(int from, int to, Consumer<? super T> f);
  abstract Iterator<T> iterator();
  abstract Iterator<T> iterator(int from, int to);
  abstract Optional<T> reduce(BinaryOperator<T> f);
  abstract <U> U reduce(U unit, BiFunction<U, T, U> f);
  abstract T[] toArray(IntFunction<T[]> allocator);
  abstract int size();

}
