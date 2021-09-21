package fx.react.value;

import fx.react.Subscription;
import javafx.beans.value.ObservableValue;

class OrElseConst<T> extends ValBase<T> {

  final ObservableValue<? extends T> src;
  final T other;

  OrElseConst(ObservableValue<? extends T> src, T other) {
    this.src = src;
    this.other = other;
  }

  @Override
  protected T computeValue() {
    var val = src.getValue();
    return val != null ? val : other;
  }

  @Override
  protected Subscription connect() {
    return Val.observeInvalidations(src, obs -> invalidate());
  }

}
