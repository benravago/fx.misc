package fx.react;

import java.util.function.BiFunction;
import java.util.function.Function;

import fx.react.util.Timer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;

class SuccessionReducingStream<I, O> extends EventStreamBase<O> implements AwaitingEventStream<O> {

  final EventStream<I> input;
  final Function<? super I, ? extends O> initial;
  final BiFunction<? super O, ? super I, ? extends O> reduction;
  final Timer timer;

  boolean hasEvent = false;
  BooleanBinding pending = null;
  O event = null;

  SuccessionReducingStream(EventStream<I> input, Function<? super I, ? extends O> initial, BiFunction<? super O, ? super I, ? extends O> reduction, Function<Runnable, Timer> timerFactory) {
    this.input = input;
    this.initial = initial;
    this.reduction = reduction;
    this.timer = timerFactory.apply(this::handleTimeout);
  }

  @Override
  public ObservableBooleanValue pendingProperty() {
    if (pending == null) {
      pending = new BooleanBinding() {
        @Override
        protected boolean computeValue() {
          return hasEvent;
        }
      };
    }
    return pending;
  }

  @Override
  public boolean isPending() {
    return pending != null ? pending.get() : hasEvent;
  }

  @Override
  protected final Subscription observeInputs() {
    return input.subscribe(this::handleEvent);
  }

  void handleEvent(I i) {
    if (hasEvent) {
      event = reduction.apply(event, i);
    } else {
      assert event == null;
      event = initial.apply(i);
      hasEvent = true;
      invalidatePending();
    }
    timer.restart();
  }

  void handleTimeout() {
    assert hasEvent;
    hasEvent = false;
    var toEmit = event;
    event = null;
    emit(toEmit);
    invalidatePending();
  }

  void invalidatePending() {
    if (pending != null) {
      pending.invalidate();
    }
  }

}
