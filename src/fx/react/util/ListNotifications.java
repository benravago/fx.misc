package fx.react.util;

import fx.react.collection.ListModificationSequence;
import fx.react.collection.Observer;
import fx.react.collection.QuasiChange;

class ListNotifications<E> extends NotificationAccumulatorBase<Observer<? super E, ?>, QuasiChange<? extends E>, ListModificationSequence<E>> implements ListChangeAccumulation<E> {

  @Override
  protected AccumulatorSize size(Observer<? super E, ?> observer, ListModificationSequence<E> accumulatedValue) {
    return observer.sizeOf(accumulatedValue);
  }

  @Override
  protected Runnable head(Observer<? super E, ?> observer, ListModificationSequence<E> mods) {
    return takeHead(observer, mods);
  }

  <O> Runnable takeHead(Observer<? super E, O> observer, ListModificationSequence<E> mods) {
    var h = observer.headOf(mods);
    return () -> observer.onChange(h);
  }

  @Override
  protected ListModificationSequence<E> tail(Observer<? super E, ?> observer, ListModificationSequence<E> mods) {
    return observer.tailOf(mods);
  }

}