package fx.react;

import java.util.function.Consumer;

import fx.react.value.ProxyVal;
import fx.react.value.Val;

class ToggleFromVal extends ProxyVal<Boolean, Boolean> implements Toggle {

  Suspendable suspender;

  ToggleFromVal(Val<Boolean> obs, Suspendable suspender) {
    super(obs);
    this.suspender = suspender;
  }

  @Override
  public Boolean getValue() {
    return getUnderlyingObservable().getValue();
  }

  @Override
  public Guard suspend() {
    return suspender.suspend();
  }

  @Override
  protected Consumer<? super Boolean> adaptObserver(Consumer<? super Boolean> observer) {
    return observer;
  }

}