package fx.react.collection;

import java.util.function.Consumer;

import fx.react.util.WrapperBase;
import javafx.collections.ObservableList;

class ModificationObserverWrapper<T> extends WrapperBase<Consumer<? super ModifiedList<? extends T>>> implements QuasiModificationObserver<T> {

  final ObservableList<T> list;

  ModificationObserverWrapper(ObservableList<T> list, Consumer<? super ModifiedList<? extends T>> delegate) {
    super(delegate);
    this.list = list;
  }

  @Override
  public void onChange(QuasiModification<? extends T> change) {
    getWrappedValue().accept(QuasiModification.instantiate(change, list));
  }

}
