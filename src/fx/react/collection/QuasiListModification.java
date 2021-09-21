package fx.react.collection;

import java.util.Collections;
import java.util.List;

class QuasiListModification<E> implements QuasiModification<E> {

  final int position;
  final List<? extends E> removed;
  final int addedSize;

  QuasiListModification(int position, List<? extends E> removed, int addedSize) {
    this.position = position;
    this.removed = Collections.unmodifiableList(removed);
    this.addedSize = addedSize;
  }

  @Override
  public int getFrom() {
    return position;
  }

  @Override
  public int getAddedSize() {
    return addedSize;
  }

  @Override
  public List<? extends E> getRemoved() {
    return removed;
  }

  @Override
  public String toString() {
    return "[modification at: " + getFrom() + ", removed: " + getRemoved() + ", added size: " + getAddedSize() + "]";
  }

}
