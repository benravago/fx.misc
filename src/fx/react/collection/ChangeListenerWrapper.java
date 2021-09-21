package fx.react.collection;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import fx.react.util.WrapperBase;

class ChangeListenerWrapper<T> extends WrapperBase<ListChangeListener<? super T>> implements QuasiChangeObserver<T> {

  final ObservableList<T> list;

  ChangeListenerWrapper(ObservableList<T> list, ListChangeListener<? super T> listener) {
    super(listener);
    this.list = list;
  }

  @Override
  public void onChange(QuasiChange<? extends T> change) {
    var modifications = change.getModifications();
    if (modifications.isEmpty()) {
      return;
    }

    getWrappedValue().onChanged(new ListChangeListener.Change<T>(list) {
      int current = -1;

      @Override
      public int getFrom() {
        return modifications.get(current).getFrom();
      }

      @Override
      protected int[] getPermutation() {
        return new int[0]; // not a permutation
      }

      /* Can change to List<? extends E> and remove unsafe cast when
       * https://javafx-jira.kenai.com/browse/RT-39683 is resolved. */
      @Override
      @SuppressWarnings("unchecked")
      public List<T> getRemoved() {
        // cast is safe, because the list is unmodifiable
        return (List<T>) modifications.get(current).getRemoved();
      }

      @Override
      public int getTo() {
        return modifications.get(current).getTo();
      }

      @Override
      public boolean next() {
        if (current + 1 < modifications.size()) {
          ++current;
          return true;
        } else {
          return false;
        }
      }

      @Override
      public void reset() {
        current = -1;
      }
    });
  }

}