package fx.react.value;

import java.util.function.Consumer;

import fx.react.Subscription;
import javafx.beans.value.ObservableValue;

class VarFromVal<T> extends ProxyVal<T, T> implements Var<T> {

  final Consumer<T> setter;
  Subscription binding = null;

  VarFromVal(Val<T> underlying, Consumer<T> setter) {
    super(underlying);
    this.setter = setter;
  }

  @Override
  public T getValue() {
    return getUnderlyingObservable().getValue();
  }

  @Override
  protected Consumer<? super T> adaptObserver(Consumer<? super T> observer) {
    return observer; // no adaptation needed
  }

  @Override
  public void bind(ObservableValue<? extends T> observable) {
    unbind();
    binding = Val.observeChanges(observable, (obs, oldVal, newVal) -> setValue(newVal));
    setValue(observable.getValue());
  }

  @Override
  public void unbind() {
    if (binding != null) {
      binding.unsubscribe();
      binding = null;
    }
  }

  @Override
  public boolean isBound() {
    return binding != null;
  }

  @Override
  public void setValue(T value) {
    setter.accept(value);
  }

}
