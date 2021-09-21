package fx.react.value;

import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class FlatMappedVar<T, U, O extends Property<U>> extends FlatMapped<T, U, O> implements Var<U> {

  final ChangeListener<O> srcListenerWhenBound;

  ObservableValue<? extends U> boundTo = null;

  FlatMappedVar(ObservableValue<T> src, Function<? super T, O> f, U resetToOnUnbind) {
    this(src, f, oldProperty -> oldProperty.setValue(resetToOnUnbind));
  }

  FlatMappedVar(ObservableValue<T> src, Function<? super T, O> f) {
    this(src, f, oldProperty -> {});
  }

  FlatMappedVar(ObservableValue<T> src, Function<? super T, O> f, Consumer<O> onUnbind) {
    super(src, f);
    srcListenerWhenBound = (obs, oldProperty, newProperty) -> {
      assert boundTo != null;
      if (oldProperty != null) {
        oldProperty.unbind();
        onUnbind.accept(oldProperty);
      }
      if (newProperty != null) {
        newProperty.bind(boundTo);
      }
    };
  }

  @Override
  public void setValue(U value) {
    src.ifPresent(sel -> {
      sel.setValue(value);
      invalidate();
    });
  }

  @Override
  public void bind(ObservableValue<? extends U> other) {
    if (other == null) {
      throw new IllegalArgumentException("Cannot bind to null");
    }
    if (boundTo == null) {
      src.addListener(srcListenerWhenBound);
    }
    src.ifPresent(sel -> sel.bind(other));
    boundTo = other;
  }

  @Override
  public void unbind() {
    if (boundTo != null) {
      src.removeListener(srcListenerWhenBound);
      src.ifPresent(Property::unbind);
      boundTo = null;
    }
  }

  @Override
  public boolean isBound() {
    return boundTo != null || (src.isPresent() && src.getOrThrow().isBound());
  }

}