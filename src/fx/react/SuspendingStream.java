package fx.react;

import java.util.function.Consumer;

class SuspendingStream<T, S extends Suspendable> extends SuspenderBase<Consumer<? super T>, T, S> implements SuspenderStream<T, S>, ProperEventStream<T> {

  final EventStream<T> source;

  public SuspendingStream(EventStream<T> source, S suspendable) {
    super(suspendable);
    this.source = source;
  }

  @Override
  protected Subscription observeInputs() {
    return source.subscribe(this::notifyObserversWhileSuspended);
  }

}
