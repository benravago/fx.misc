package fx.react;

import fx.react.util.AccumulationFacility;
import fx.react.util.AccumulatorSize;
import fx.react.util.NotificationAccumulator;

public abstract class SuspendableBase<O, T, A> extends ObservableBase<O, T> implements Suspendable {

  final EventStream<T> input;
  final AccumulationFacility<T, A> af;

  int suspended = 0;
  boolean hasValue = false;
  A accumulatedValue = null;

  protected SuspendableBase(EventStream<T> input, NotificationAccumulator<O, T, A> pn) {
    super(pn);
    this.input = input;
    this.af = pn.getAccumulationFacility();
  }

  protected abstract AccumulatorSize sizeOf(A accum);

  protected abstract T headOf(A accum);

  protected abstract A tailOf(A accum);

  protected A initialAccumulator(T value) {
    return af.initialAccumulator(value);
  }

  protected A reduce(A accum, T value) {
    return af.reduce(accum, value);
  }

  protected final boolean isSuspended() {
    return suspended > 0;
  }

  @Override
  public Guard suspend() {
    ++suspended;
    return Guard.closeableOnce(this::resume);
  }

  @Override
  protected Subscription observeInputs() {
    var sub = input.subscribe(this::handleEvent);
    return sub.and(this::reset);
  }

  void resume() {
    --suspended;
    if (suspended == 0 && hasValue) {
      while (sizeOf(accumulatedValue) == AccumulatorSize.MANY) {
        enqueueNotifications(headOf(accumulatedValue));
        accumulatedValue = tailOf(accumulatedValue);
      }
      if (sizeOf(accumulatedValue) == AccumulatorSize.ONE) {
        enqueueNotifications(headOf(accumulatedValue));
      }
      reset();
      notifyObservers();
    }
  }

  void reset() {
    hasValue = false;
    accumulatedValue = null;
  }

  void handleEvent(T event) {
    if (isSuspended()) {
      if (hasValue) {
        accumulatedValue = reduce(accumulatedValue, event);
      } else {
        accumulatedValue = initialAccumulator(event);
        hasValue = true;
      }
    } else {
      notifyObservers(event);
    }
  }

}
