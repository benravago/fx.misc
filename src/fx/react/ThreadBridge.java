package fx.react;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

class ThreadBridge<T> extends EventStreamBase<T> {

  final EventStream<T> input;
  final Executor sourceThreadExecutor;
  final Executor targetThreadExecutor;

  ThreadBridge(EventStream<T> input, Executor sourceThreadExecutor, Executor targetThreadExecutor) {
    this.input = input;
    this.sourceThreadExecutor = sourceThreadExecutor;
    this.targetThreadExecutor = targetThreadExecutor;
  }

  @Override
  protected Subscription observeInputs() {
    var subscription = new CompletableFuture<Subscription>();
    sourceThreadExecutor.execute(() -> {
      subscription.complete(input.subscribe(e -> {
        targetThreadExecutor.execute(() -> emit(e));
      }));
    });
    return () -> subscription.thenAcceptAsync(Subscription::unsubscribe, sourceThreadExecutor);
  }

}
