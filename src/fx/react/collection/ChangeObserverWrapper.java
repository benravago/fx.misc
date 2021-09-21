package fx.react.collection;

import java.util.function.Consumer;

import fx.react.util.WrapperBase;
import javafx.collections.ObservableList;

class ChangeObserverWrapper<T> extends WrapperBase<Consumer<? super ListChange<? extends T>>> implements QuasiChangeObserver<T> {

  final ObservableList<T> list;

  ChangeObserverWrapper(ObservableList<T> list, Consumer<? super ListChange<? extends T>> delegate) {
    super(delegate);
    this.list = list;
  }

  @Override
  public void onChange(QuasiChange<? extends T> change) {
    getWrappedValue().accept(QuasiChange.instantiate(change, list));
  }

}