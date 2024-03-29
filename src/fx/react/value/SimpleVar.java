package fx.react.value;

import java.util.Objects;

import fx.react.Subscription;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;

class SimpleVar<T> extends ValBase<T> implements Var<T> {

  final InvalidationListener boundToListener = obs -> invalidate();

  T value;
  ObservableValue<? extends T> boundTo = null;

  SimpleVar(T initialValue) {
    this.value = initialValue;
  }

  @Override
  public void bind(ObservableValue<? extends T> other) {
    if (other == null) {
      throw new IllegalArgumentException("Cannot bind to null");
    }
    if (boundTo != null) {
      boundTo.removeListener(boundToListener);
    }
    boundTo = other;
    if (isObservingInputs()) {
      boundTo.addListener(boundToListener);
    }
    invalidate();
  }

  @Override
  public void unbind() {
    if (boundTo != null) {
      boundTo.removeListener(boundToListener);
      boundTo = null;
    }
  }

  @Override
  public boolean isBound() {
    return boundTo != null;
  }

  @Override
  public void setValue(T value) {
    if (isBound()) {
      throw new IllegalStateException("Cannot set a bound property");
    } else {
      if (!Objects.equals(value, this.value)) {
        this.value = value;
        invalidate();
      }
    }
  }

  @Override
  protected Subscription connect() {
    if (boundTo != null) {
      boundTo.addListener(boundToListener);
    }
    return () -> {
      if (boundTo != null) {
        boundTo.removeListener(boundToListener);
      }
    };
  }

  @Override
  protected T computeValue() {
    if (isBound()) {
      value = boundTo.getValue();
    }
    return value;
  }

}
