package fx.react.value;

import java.util.function.Function;

import fx.react.Subscription;
import javafx.beans.value.ObservableValue;

abstract class FlatMapped<T, U, O extends ObservableValue<U>> extends ValBase<U> {

  final Val<O> src;

  Subscription selectedSubscription = null; // irrelevant when not connected

  FlatMapped(ObservableValue<T> src, Function<? super T, O> f) {
    this.src = Val.map(src, f);
  }

  @Override
  protected final Subscription connect() {
    return Val.observeInvalidations(src, obs -> srcInvalidated()).and(this::stopObservingSelected);
  }

  @Override
  protected final U computeValue() {
    if (isObservingInputs()) {
      startObservingSelected();
    }
    return src.getOpt().map(O::getValue).orElse(null);
  }

  void startObservingSelected() {
    assert isObservingInputs();
    if (selectedSubscription == null) {
      src.ifPresent(sel -> {
        selectedSubscription = Val.observeInvalidations(sel, obs -> selectedInvalidated());
      });
    }
  }

  void stopObservingSelected() {
    if (selectedSubscription != null) {
      selectedSubscription.unsubscribe();
      selectedSubscription = null;
    }
  }

  void selectedInvalidated() {
    invalidate();
  }

  void srcInvalidated() {
    stopObservingSelected();
    invalidate();
  }

}