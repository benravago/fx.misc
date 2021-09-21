package fx.react;

import fx.react.value.ValBase;
import javafx.beans.value.ObservableBooleanValue;

abstract class SuspendableBoolean extends ValBase<Boolean> implements ObservableBooleanValue, Toggle {

  int suspenders = 0;

  @Override
  public Guard suspend() {
    if (++suspenders == 1) {
      invalidate();
    }
    return ((Guard) this::release).closeableOnce();
  }

  void release() {
    assert suspenders > 0;
    if (--suspenders == 0) {
      invalidate();
    }
  }

  public EventStream<?> yeses() {
    return EventStreams.valuesOf(this).filterMap(val -> !val, val -> null);
  }

  public EventStream<?> noes() {
    return EventStreams.valuesOf(this).filterMap(val -> val, val -> null);
  }

  protected final boolean isSuspended() {
    return suspenders > 0;
  }

  @Override
  protected final Subscription connect() {
    return Subscription.EMPTY;
  }

  @Override
  protected final Boolean computeValue() {
    return get();
  }

}