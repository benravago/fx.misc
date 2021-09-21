package fx.react.value;

import java.util.function.Consumer;

import fx.react.ProxyObservable;

public abstract class ProxyVal<T, U> extends ProxyObservable<Consumer<? super T>, Consumer<? super U>, Val<U>> implements Val<T> {

  protected ProxyVal(Val<U> underlying) {
    super(underlying);
  }

}
