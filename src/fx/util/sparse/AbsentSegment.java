package fx.util.sparse;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import fx.util.Lists;

class AbsentSegment<E> implements Segment<E> {

  int length;

  AbsentSegment(int length) {
    assert length > 0;
    this.length = length;
  }

  @Override
  public String toString() {
    return "[Void x " + length + "]";
  }

  @Override
  public boolean isPresent() {
    return false;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getPresentCount() {
    return 0;
  }

  @Override
  public int getPresentCountBetween(int from, int to) {
    return 0;
  }

  @Override
  public boolean isPresent(int index) {
    return false;
  }

  @Override
  public Optional<E> get(int index) {
    return Optional.empty();
  }

  @Override
  public E getOrThrow(int index) {
    throw new NoSuchElementException();
  }

  @Override
  public void setOrThrow(int index, E elem) {
    throw new NoSuchElementException();
  }

  @Override
  public List<E> appendTo(List<E> acc) {
    return acc;
  }

  @Override
  public List<E> appendRangeTo(List<E> acc, int from, int to) {
    return acc;
  }

  @Override
  public Segment<E> subSegment(int from, int to) {
    assert Lists.isValidRange(from, to, length);
    return new AbsentSegment<>(to - from);
  }

  @Override
  public boolean possiblyDestructiveAppend(Segment<E> suffix) {
    if (suffix.getPresentCount() == 0) {
      length += suffix.getLength();
      return true;
    } else {
      return false;
    }
  }

}