package fx.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;

class SingleItemList<T> extends Vector<T> {

  final T elem;

  SingleItemList(T elem) {
    this.elem = elem;
  }

  @Override
  T get(int index) {
    assert index == 0;
    return elem;
  }

  @Override
  Vector<T> add(T elem) {
    return new MultiItemList<>(this.elem, elem);
  }

  @Override
  Vector<T> remove(T elem) {
    return (Objects.equals(this.elem, elem)) ? null : this;
  }

  @Override
  void forEach(Consumer<? super T> f) {
    f.accept(elem);
  }

  @Override
  void forEachBetween(int from, int to, Consumer<? super T> f) {
    assert from == 0 && to == 1;
    f.accept(elem);
  }

  @Override
  Iterator<T> iterator() {
    return new Iterator<T>() {
      boolean hasNext = true;
      @Override
      public boolean hasNext() {
        return hasNext;
      }
      @Override
      public T next() {
        if (hasNext) {
          hasNext = false;
          return elem;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  @Override
  Iterator<T> iterator(int from, int to) {
    assert from == 0 && to == 1;
    return iterator();
  }

  @Override
  Optional<T> reduce(BinaryOperator<T> f) {
    return Optional.of(elem);
  }

  @Override
  <U> U reduce(U unit, BiFunction<U, T, U> f) {
    return f.apply(unit, elem);
  }

  @Override
  T[] toArray(IntFunction<T[]> allocator) {
    var res = allocator.apply(1);
    res[0] = elem;
    return res;
  }

  @Override
  int size() {
    return 1;
  }

}