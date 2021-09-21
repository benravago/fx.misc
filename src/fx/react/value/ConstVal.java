package fx.react.value;

import java.util.function.Consumer;

import fx.react.RigidObservable;

class ConstVal<T> extends RigidObservable<Consumer<? super T>> implements Val<T> {

  final T value;

  ConstVal(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }

}
