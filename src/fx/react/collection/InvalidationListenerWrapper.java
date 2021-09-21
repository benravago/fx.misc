package fx.react.collection;

import fx.react.util.WrapperBase;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;

class InvalidationListenerWrapper<T> extends WrapperBase<InvalidationListener> implements QuasiChangeObserver<T> {

  final ObservableList<T> list;

  InvalidationListenerWrapper(ObservableList<T> list, InvalidationListener listener) {
    super(listener);
    this.list = list;
  }

  @Override
  public void onChange(QuasiChange<? extends T> change) {
    getWrappedValue().invalidated(list);
  }

}