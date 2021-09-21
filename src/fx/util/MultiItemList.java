package fx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;

class MultiItemList<T> extends Vector<T> {

  final List<T> elems;

  // when > 0, this ListHelper must be immutable,
  // i.e. use copy-on-write for mutating operations
  int iterating = 0;

  @SafeVarargs
  MultiItemList(T... elems) {
    this(Arrays.asList(elems));
  }

  MultiItemList(List<T> elems) {
    this.elems = new ArrayList<>(elems);
  }

  MultiItemList<T> copy() {
    return new MultiItemList<>(elems);
  }

  @Override
  T get(int index) {
    return elems.get(index);
  }

  @Override
  Vector<T> add(T elem) {
    if (iterating > 0) {
      return copy().add(elem);
    } else {
      elems.add(elem);
      return this;
    }
  }

  @Override
  Vector<T> remove(T elem) {
    var idx = elems.indexOf(elem);
    if (idx == -1) {
      return this;
    } else {
      return switch (elems.size()) {
        case 0, 1 -> {
          throw new AssertionError();
        }
        case 2 -> {
          yield new SingleItemList<>(elems.get(1 - idx));
        }
        default -> {
          if (iterating > 0) {
            yield copy().remove(elem);
          } else {
            elems.remove(elem);
            yield this;
          }
        }
      };
    }
  }

  @Override
  void forEach(Consumer<? super T> f) {
    ++iterating;
    try {
      elems.forEach(f);
    } finally {
      --iterating;
    }
  }

  @Override
  void forEachBetween(int from, int to, Consumer<? super T> f) {
    ++iterating;
    try {
      elems.subList(from, to).forEach(f);
    } finally {
      --iterating;
    }
  }

  @Override
  Iterator<T> iterator() {
    return iterator(0, elems.size());
  }

  @Override
  Iterator<T> iterator(int from, int to) {
    assert from < to;
    ++iterating;
    return new Iterator<T>() {
      int next = from;
      @Override
      public boolean hasNext() {
        return next < to;
      }
      @Override
      public T next() {
        if (next < to) {
          var res = elems.get(next);
          ++next;
          if (next == to) {
            --iterating;
          }
          return res;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  @Override
  Optional<T> reduce(BinaryOperator<T> f) {
    return elems.stream().reduce(f);
  }

  @Override
  <U> U reduce(U unit, BiFunction<U, T, U> f) {
    var u = unit;
    for (var elem : elems) {
      u = f.apply(u, elem);
    }
    return u;
  }

  @Override
  T[] toArray(IntFunction<T[]> allocator) {
    return elems.toArray(allocator.apply(size()));
  }

  @Override
  int size() {
    return elems.size();
  }

}