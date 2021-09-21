package fx.react.collection;

import java.util.List;

class MaterializedListModification<E> implements MaterializedModification<E> {

  final int from;
  final List<? extends E> removed;
  final List<? extends E> added;

  MaterializedListModification(int from, List<? extends E> removed, List<? extends E> added) {
    this.from = from;
    this.removed = removed;
    this.added = added;
  }

  @Override
  public int getFrom() {
    return from;
  }

  @Override
  public List<? extends E> getRemoved() {
    return removed;
  }

  @Override
  public List<? extends E> getAdded() {
    return added;
  }

}
