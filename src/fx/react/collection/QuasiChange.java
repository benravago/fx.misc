package fx.react.collection;

import fx.util.Lists;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public interface QuasiChange<E> extends ListModificationSequence<E> {

  @Override
  default QuasiChange<E> asListChange() {
    return this;
  }

  @Override
  default ListChangeAccumulator<E> asListChangeAccumulator() {
    return new ListChangeAccumulator<>(this);
  }

  @SuppressWarnings("unchecked")
  static <E> QuasiChange<E> safeCast(QuasiChange<? extends E> mod) {
    // the cast is safe, because instances are immutable
    return (QuasiChange<E>) mod;
  }

  static <E> QuasiChange<E> from(Change<? extends E> ch) {
    var res = new QuasiListChange<E>();
    while (ch.next()) {
      res.add(QuasiModification.fromCurrentStateOf(ch));
    }
    return res;
  }

  static <E> ListChange<E> instantiate(QuasiChange<? extends E> change, ObservableList<E> list) {
    return () -> Lists.<QuasiModification<? extends E>, ModifiedList<? extends E>>mappedView(
      change.getModifications(), mod -> QuasiModification.instantiate(mod, list));
  }

}