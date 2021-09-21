package fx.react.value;

import java.util.function.Function;

import fx.react.Subscription;
import javafx.beans.value.ObservableValue;

class MappedVal<T, U> extends ValBase<U> {

  final ObservableValue<T> src;
  final Function<? super T, ? extends U> f;

  MappedVal(ObservableValue<T> src, Function<? super T, ? extends U> f) {
    this.src = src;
    this.f = f;
  }

  @Override
  protected U computeValue() {
    var baseVal = src.getValue();
    return baseVal != null ? f.apply(baseVal) : null;
  }

  @Override
  protected Subscription connect() {
    return Val.observeInvalidations(src, obs -> invalidate());
  }

}