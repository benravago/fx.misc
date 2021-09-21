package fx.react.value;

import fx.react.Subscription;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;

class ValWrapper<T, D extends ObservableValue<T>> extends ValBase<T> {

  final D delegate;

  ValWrapper(D delegate) {
    this.delegate = delegate;
  }

  D getDelegate() {
    return delegate;
  }

  @Override
  protected Subscription connect() {
    InvalidationListener listener = obs -> invalidate();
    delegate.addListener(listener);
    return () -> delegate.removeListener(listener);
  }

  @Override
  protected T computeValue() {
    return delegate.getValue();
  }

}