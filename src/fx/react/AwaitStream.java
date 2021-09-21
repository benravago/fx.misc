package fx.react;

import static javafx.concurrent.WorkerStateEvent.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import fx.util.Try;
import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;

class AwaitStream<T, F> extends EventStreamBase<Try<T>> implements AwaitingEventStream<Try<T>> {

  static <T> AwaitingEventStream<Try<T>> awaitCompletionStage(EventStream<CompletionStage<T>> source, Executor clientThreadExecutor) {
    return new AwaitStream<>(source, (future, handler) -> addCompletionHandler(future, handler, clientThreadExecutor));
  }

  static <T> AwaitingEventStream<Try<T>> awaitTask(EventStream<Task<T>> source) {
    return new AwaitStream<>(source, AwaitStream::addCompletionHandler);
  }

  static <T> void addCompletionHandler(CompletionStage<T> future, Handler<T, Throwable, Boolean> handler, Executor executor) {
    future.whenCompleteAsync((result, error) -> handler.accept(result, error, false), executor);
  }

  static <T> void addCompletionHandler(Task<T> t, Handler<T, Throwable, Boolean> handler) {
    t.addEventHandler(WORKER_STATE_SUCCEEDED, e -> handler.accept(t.getValue(), null, false));
    t.addEventHandler(WORKER_STATE_FAILED, e -> handler.accept(null, t.getException(), false));
    t.addEventHandler(WORKER_STATE_CANCELLED, e -> handler.accept(null, null, true));
  }

  final EventStream<F> source;
  final SuspendableNo pending = new SuspendableNo();
  final BiConsumer<F, Handler<T, Throwable, Boolean>> addCompletionHandler;

  AwaitStream(EventStream<F> source, BiConsumer<F, Handler<T, Throwable, Boolean>> addCompletionHandler) {
    this.source = source;
    this.addCompletionHandler = addCompletionHandler;
  }

  @Override
  public final ObservableBooleanValue pendingProperty() {
    return pending;
  }

  @Override
  public final boolean isPending() {
    return pending.get();
  }

  @Override
  protected final Subscription observeInputs() {
    return source.subscribe(future -> {
      var g = pending.suspend();
      addCompletionHandler.accept(future, (result, error, cancelled) -> {
        if (!cancelled) {
          emit(error == null ? Try.success(result) : Try.failure(error));
        }
        g.close();
      });
    });
  }

}
