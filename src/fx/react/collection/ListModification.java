package fx.react.collection;

import java.util.List;

import fx.react.util.Lists;
import javafx.collections.ObservableList;

class ListModification<E> implements ModifiedList<E> {

  int position;
  List<? extends E> removed;
  int addedSize;
  final ObservableList<E> list;

  ListModification(int position, List<? extends E> removed, int addedSize, ObservableList<E> list) {
    this.position = position;
    this.removed = removed;
    this.addedSize = addedSize;
    this.list = list;
  }

  @Override
  public int getFrom() {
    return position;
  }

  @Override
  public List<? extends E> getRemoved() {
    return removed;
  }

  @Override
  public List<? extends E> getAddedSubList() {
    return list.subList(position, position + addedSize);
  }

  @Override
  public int getAddedSize() {
    return addedSize;
  }

  @Override
  public String toString() {
    return "[modification at: " + getFrom() + ", removed: " + getRemoved() + ", added size: " + getAddedSize() + "]";
  }

  @Override
  public ModifiedList<E> trim() {
    var t = Lists.commonPrefixSuffixLengths(removed, getAddedSubList());
    var pref = t[0];
    var suff = t[1];
    return (pref == 0 && suff == 0)
      ? this : new ListModification<>(position + pref, removed.subList(pref, getRemovedSize() - suff), addedSize - pref - suff, list);
  }

}
