package fx.react.collection;

import java.util.AbstractList;
import java.util.List;

class SubList<E> extends AbstractList<E> {

  final List<E> list;
  final int from;
  final int to;

  SubList(List<E> list, int from, int to) {
    if (from < 0 || from > to || to > list.size()) {
      throw new IndexOutOfBoundsException("0 <= " + from + " <= " + to + " <= " + list.size());
    }
    this.list = list;
    this.from = from;
    this.to = to;
  }

  @Override
  public int size() {
    return to - from;
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    return list.get(from + index);
  }

  @Override
  public E set(int index, E element) {
    checkIndex(index);
    return list.set(from + index, element);
  }

  @Override
  public void add(int index, E element) {
    if (index < 0 || index > size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    list.add(from + index, element);
  }

  @Override
  public E remove(int index) {
    checkIndex(index);
    return list.remove(from + index);
  }

  void checkIndex(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
  }

}
