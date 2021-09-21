package fx.react.collection;

import fx.react.ObservableBase;

public abstract class LiveListBase<E> extends ObservableBase<Observer<? super E, ?>, QuasiChange<? extends E>> implements ProperLiveList<E>, ListMethods<E> {
  //
}
