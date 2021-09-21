package fx.react;

import java.util.function.Consumer;

import fx.react.util.NotificationAccumulator;

abstract class SuspendableEventStreamBase<T, A> extends SuspendableBase<Consumer<? super T>, T, A> implements ProperEventStream<T>, SuspendableEventStream<T> {

  protected SuspendableEventStreamBase(EventStream<T> source, NotificationAccumulator<Consumer<? super T>, T, A> pn) {
    super(source, pn);
  }

}
