package fx.react.value;

import java.util.function.Consumer;

import fx.react.util.WrapperBase;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;

class InvalidationListenerWrapper<T> extends WrapperBase<InvalidationListener> implements Consumer<T> {

  final ObservableValue<T> obs;

  InvalidationListenerWrapper(ObservableValue<T> obs, InvalidationListener listener) {
    super(listener);
    this.obs = obs;
  }

  @Override
  public void accept(T oldValue) {
    getWrappedValue().invalidated(obs);
  }

}