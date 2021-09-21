package fx.react.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public interface QuasiModification<E> extends ListModificationLike<E> {

  static <E> QuasiModification<E> create(int position, List<? extends E> removed, int addedSize) {
    return new QuasiListModification<>(position, removed, addedSize);
  }

  static <E, F extends E> QuasiModification<E> fromCurrentStateOf(Change<F> ch) {
    var list = ch.getList();
    var from = ch.getFrom();
    var addedSize = ch.getTo() - from; // use (to - from), because ch.getAddedSize() is 0 on permutation

    List<F> removed;
    if (ch.wasPermutated()) {
      removed = new ArrayList<>(addedSize);
      for (var i = 0; i < addedSize; ++i) {
        var pi = ch.getPermutation(from + i);
        removed.add(list.get(pi));
      }
    } else {
      removed = ch.getRemoved();
    }
    return new QuasiListModification<>(from, removed, addedSize);
  }

  static <E> ModifiedList<E> instantiate(QuasiModification<? extends E> template, ObservableList<E> list) {
    return new ListModification<>(template.getFrom(), template.getRemoved(), template.getAddedSize(), list);
  }

  static <E> MaterializedModification<E> materialize(QuasiModification<? extends E> template, ObservableList<E> list) {
    return MaterializedModification.create(template.getFrom(), template.getRemoved(),
      new ArrayList<>(list.subList(template.getFrom(), template.getTo())));
  }

  default ModifiedList<E> instantiate(ObservableList<E> list) {
    return instantiate(this, list);
  }

  default MaterializedModification<E> materialize(ObservableList<E> list) {
    return materialize(this, list);
  }

  default QuasiChange<E> asListChange() {
    return () -> Collections.singletonList(this);
  }

}