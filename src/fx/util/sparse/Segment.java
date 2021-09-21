package fx.util.sparse;

import java.util.List;
import java.util.Optional;

interface Segment<E> {

  boolean isPresent();

  int getLength();

  int getPresentCount();

  int getPresentCountBetween(int from, int to);

  boolean isPresent(int index);

  Optional<E> get(int index);

  E getOrThrow(int index);

  void setOrThrow(int index, E elem);

  List<E> appendTo(List<E> acc);

  List<E> appendRangeTo(List<E> acc, int from, int to);

  Segment<E> subSegment(int from, int to);

  boolean possiblyDestructiveAppend(Segment<E> suffix);

  default Stats getStatsBetween(int from, int to) {
    return new Stats(to - from, getPresentCountBetween(from, to));
  }

}