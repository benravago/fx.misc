package fx.react.collection;

import java.util.Collection;

/**
 * Trait to be mixed into implementations of unmodifiable lists.
 * Provides default implementations of mutating list methods.
 */
interface UnmodifiableByDefaultList<E> extends ListMethods<E> {

  @Override
  default E set(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  default void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  default E remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean add(E e) {
    add(size(), e);
    return true;
  }

  @Override
  default boolean addAll(Collection<? extends E> c) {
    for (var e : c) {
      add(e);
    }
    return !c.isEmpty();
  }

  @Override
  default boolean addAll(int index, Collection<? extends E> c) {
    for (var e : c) {
      add(index++, e);
    }
    return !c.isEmpty();
  }

  @Override
  default boolean remove(Object o) {
    var i = indexOf(o);
    if (i != -1) {
      remove(i);
      return true;
    } else {
      return false;
    }
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    return c.stream().anyMatch(this::remove);
  }

  @Override
  default boolean retainAll(Collection<?> c) {
    var changed = false;
    for (var i = size() - 1; i >= 0; --i) {
      if (!c.contains(get(i))) {
        remove(i);
        changed = true;
      }
    }
    return changed;
  }

  @Override
  default void clear() {
    for (var i = size() - 1; i >= 0; --i) {
      remove(i);
    }
  }

}
