package fx.react.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Plain boilerplate, because java.util.List does not have default methods.
 */
interface ListMethods<E> extends List<E> {

  @Override
  default boolean isEmpty() {
    return size() == 0;
  }

  @Override
  default int indexOf(Object o) {
    for (var i = 0; i < size(); ++i) {
      if (Objects.equals(o, get(i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  default int lastIndexOf(Object o) {
    for (var i = size() - 1; i >= 0; ++i) {
      if (Objects.equals(o, get(i))) {
        return i;
      }
    }
    return -1;
  }

  @Override
  default boolean contains(Object o) {
    return indexOf(o) != -1;
  }

  @Override
  default boolean containsAll(Collection<?> c) {
    return c.stream().allMatch(this::contains);
  }

  @Override
  default Iterator<E> iterator() {
    return listIterator();
  }

  @Override
  default ListIterator<E> listIterator() {
    return new ListIteration<>(this);
  }

  @Override
  default ListIterator<E> listIterator(int index) {
    return new ListIteration<>(this, index);
  }

  @Override
  default List<E> subList(int fromIndex, int toIndex) {
    return new SubList<>(this, fromIndex, toIndex);
  }

  @Override
  default Object[] toArray() {
    var res = new Object[size()];
    var i = 0;
    for (var elem : this) {
      res[i++] = elem;
    }
    return res;
  }

  @Override
  default <T> T[] toArray(T[] a) {
    return new ArrayList<E>(this).toArray(a); // screw it
  }

}
