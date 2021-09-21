package fx.util.sparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import fx.util.Lists;

class PresentSegment<E> implements Segment<E> {

  final List<E> list;

  PresentSegment(Collection<? extends E> c) {
    assert c.size() > 0;
    list = new ArrayList<>(c);
  }

  @Override
  public String toString() {
    return "[" + list.size() + " items: " + list + "]";
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public int getLength() {
    return list.size();
  }

  @Override
  public int getPresentCount() {
    return list.size();
  }

  @Override
  public int getPresentCountBetween(int from, int to) {
    assert Lists.isValidRange(from, to, getLength());
    return to - from;
  }

  @Override
  public boolean isPresent(int index) {
    assert Lists.isValidIndex(index, getLength());
    return true;
  }

  @Override
  public Optional<E> get(int index) {
    return Optional.of(list.get(index));
  }

  @Override
  public E getOrThrow(int index) {
    return list.get(index);
  }

  @Override
  public void setOrThrow(int index, E elem) {
    list.set(index, elem);
  }

  @Override
  public List<E> appendTo(List<E> acc) {
    acc.addAll(list);
    return acc;
  }

  @Override
  public List<E> appendRangeTo(List<E> acc, int from, int to) {
    acc.addAll(list.subList(from, to));
    return acc;
  }

  @Override
  public Segment<E> subSegment(int from, int to) {
    return new PresentSegment<>(list.subList(from, to));
  }

  @Override
  public boolean possiblyDestructiveAppend(Segment<E> suffix) {
    if (suffix.getPresentCount() == suffix.getLength()) {
      suffix.appendTo(list);
      return true;
    } else {
      return false;
    }
  }

}