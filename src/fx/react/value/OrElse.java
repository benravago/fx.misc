package fx.react.value;

import fx.react.Subscription;
import javafx.beans.value.ObservableValue;

class OrElse<T> extends ValBase<T> {

  final ObservableValue<? extends T> src;
  final ObservableValue<? extends T> other;

  boolean trySrc; // irrelevant when not isConnected()

  OrElse(ObservableValue<? extends T> src, ObservableValue<? extends T> other) {
    this.src = src;
    this.other = other;
  }

  @Override
  protected Subscription connect() {
    trySrc = true;
    var sub1 = Val.observeInvalidations(src, obs -> {
      trySrc = true;
      invalidate();
    });
    var sub2 = Val.observeInvalidations(other, obs -> {
      if (!trySrc) {
        invalidate();
      }
    });
    return sub1.and(sub2);
  }

  @Override
  protected T computeValue() {
    if (!isObservingInputs()) {
      var val = src.getValue();
      return val != null ? val : other.getValue();
    } else {
      if (trySrc) {
        var val = src.getValue();
        if (val != null) {
          return val;
        } else {
          trySrc = false;
        }
      }
      return other.getValue();
    }
  }

}
