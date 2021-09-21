package fx.react;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;

import fx.util.Try;

class AwaitLatestStream<T, F> extends EventStreamBase<Try<T>> implements AwaitingEventStream<Try<T>> {

  static <T> AwaitingEventStream<Try<T>> awaitCompletionStage(EventStream<CompletionStage<T>> source, Executor clientThreadExecutor) {
    return new AwaitLatestStream<>(source, EventStreams.never(), // no cancel impulse
      future -> {}, // cannot cancel a CompletionStage
      (future, handler) -> AwaitStream.addCompletionHandler(future, handler, clientThreadExecutor));
  }

  static <T> AwaitingEventStream<Try<T>> awaitTask(EventStream<Task<T>> source) {
    return new AwaitLatestStream<>(source, EventStreams.never(), // no cancel impulse
      Task::cancel, AwaitStream::addCompletionHandler);
  }

  static <T> AwaitingEventStream<Try<T>> awaitCompletionStage(EventStream<CompletionStage<T>> source, EventStream<?> cancelImpulse, Executor clientThreadExecutor) {
    return new AwaitLatestStream<>(source, cancelImpulse,
      future -> {}, // cannot cancel a CompletionStage
      (future, handler) -> AwaitStream.addCompletionHandler(future, handler, clientThreadExecutor));
  }

  static <T> AwaitingEventStream<Try<T>> awaitTask(EventStream<Task<T>> source, EventStream<?> cancelImpulse) {
    return new AwaitLatestStream<>(source, cancelImpulse, Task::cancel, AwaitStream::addCompletionHandler);
  }

  final EventStream<F> source;
  final EventStream<?> cancelImpulse;
  final Consumer<F> canceller;
  final BiConsumer<F, Handler<T, Throwable, Boolean>> addCompletionHandler;

  long revision = 0;
  F expectedFuture = null;

  BooleanBinding pending = null;

  AwaitLatestStream(EventStream<F> source, EventStream<?> cancelImpulse, Consumer<F> canceller, BiConsumer<F, Handler<T, Throwable, Boolean>> addCompletionHandler) {
    this.source = source;
    this.cancelImpulse = cancelImpulse;
    this.canceller = canceller;
    this.addCompletionHandler = addCompletionHandler;
  }

  @Override
  public ObservableBooleanValue pendingProperty() {
    if (pending == null) {
      pending = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
          return expectedFuture != null;
        }
      };
    }
    return pending;
  }

  @Override
  public boolean isPending() {
    return pending != null ? pending.get() : expectedFuture != null;
  }

  @Override
  protected Subscription observeInputs() {
    var s1 = source.subscribe(future -> {
      var rev = replaceExpected(future);
      addCompletionHandler.accept(future, (result, error, cancelled) -> {
        if (rev == revision) {
          if (!cancelled) {
            // emit before setting pending to false
            emit(error == null ? Try.success(result) : Try.failure(error));
          }
          setExpected(null);
        }
      });
    });
    var s2 = cancelImpulse.subscribe(x -> replaceExpected(null));
    return s1.and(s2);
  }

  long replaceExpected(F newExpected) {
    ++revision; // increment before cancelling, so that the cancellation handler is not executed
    if (expectedFuture != null) {
      canceller.accept(expectedFuture);
    }
    setExpected(newExpected);
    return revision;
  }

  void setExpected(F newExpected) {
    expectedFuture = newExpected;
    if (pending != null) {
      pending.invalidate();
    }
  }

}
