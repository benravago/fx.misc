package fx.react.value;

import java.util.function.Consumer;

import fx.react.SuspendableBase;
import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

class SuspendableValWrapper<T> extends SuspendableBase<Consumer<? super T>, T, T> implements SuspendableVal<T>, ProperVal<T> {

  final Val<T> delegate;

  protected SuspendableValWrapper(Val<T> obs) {
    super(obs.invalidations(), NotificationAccumulator.retainOldestValNotifications());
    this.delegate = obs;
  }

  @Override
  public T getValue() {
    return delegate.getValue();
  }

  @Override
  protected AccumulatorSize sizeOf(T accum) {
    return AccumulatorSize.ONE;
  }

  @Override
  protected T headOf(T accum) {
    return accum;
  }

  @Override
  protected T tailOf(T accum) {
    throw new UnsupportedOperationException("Cannot take a tail of a single value");
  }

}
