package fx.react.value;

import java.util.Objects;
import java.util.function.Consumer;

import fx.react.util.WrapperBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class ChangeListenerWrapper<T> extends WrapperBase<ChangeListener<? super T>> implements Consumer<T> {

  final ObservableValue<T> obs;

  ChangeListenerWrapper(ObservableValue<T> obs, ChangeListener<? super T> listener) {
    super(listener);
    this.obs = obs;
  }

  @Override
  public void accept(T oldValue) {
    var newValue = obs.getValue();
    if (!Objects.equals(oldValue, newValue)) {
      getWrappedValue().changed(obs, oldValue, newValue);
    }
  }

}