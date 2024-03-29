package fx.react;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;

class StreamBinding<T> extends ObservableValueBase<T> implements Binding<T> {

  final Subscription subscription;
  T value;

  StreamBinding(EventStream<T> input, T initialValue) {
    value = initialValue;
    subscription = input.subscribe(evt -> {
      value = evt;
      fireValueChangedEvent();
    });
  }

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public void dispose() {
    subscription.unsubscribe();
  }

  @Override
  public ObservableList<?> getDependencies() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void invalidate() {
    // do nothing
  }

  @Override
  public boolean isValid() {
    return true;
  }

}