package fx.react.collection;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

class ListIteration<E> implements ListIterator<E> {

  final List<E> list;
  int position;

  ListIteration(List<E> list, int initialPosition) {
    this.list = list;
    this.position = initialPosition;
  }

  ListIteration(List<E> list) {
    this(list, 0);
  }

  @Override
  public boolean hasNext() {
    return position < list.size();
  }

  @Override
  public E next() {
    if (position < list.size()) {
      return list.get(position++);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public boolean hasPrevious() {
    return position > 0;
  }

  @Override
  public E previous() {
    if (position > 0) {
      return list.get(--position);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public int nextIndex() {
    return position;
  }

  @Override
  public int previousIndex() {
    return position - 1;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(E e) {
    throw new UnsupportedOperationException();
  }

}
