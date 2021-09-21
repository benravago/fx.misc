package fx.react.util;

import java.util.Iterator;

abstract class NotificationAccumulatorBase<O, V, A> implements NotificationAccumulator<O, V, A>, AccumulationFacility<V, A> {

  AccumulationMap<O, V, A> accuMap = AccumulationMap.empty();

  protected abstract AccumulatorSize size(O observer, A accumulatedValue);

  protected abstract Runnable head(O observer, A accumulatedValue);

  protected abstract A tail(O observer, A accumulatedValue);

  @Override
  public AccumulationFacility<V, A> getAccumulationFacility() {
    return this;
  }

  @Override
  public boolean isEmpty() {
    return accuMap.isEmpty();
  }

  @Override
  public Runnable takeOne() {
    var t = accuMap.peek(this);
    var k = t.getKey();
    var v = t.getValue();
    return switch (size(k,v)) {
      case ZERO -> {
        accuMap = accuMap.dropPeeked();
        yield () -> {};
      }
      case ONE -> {
        accuMap = accuMap.dropPeeked();
        yield head(k,v);
      }
      case MANY -> {
        var notification = head(k,v);
        var newAccumulatedValue = tail(k,v);
        accuMap = accuMap.updatePeeked(newAccumulatedValue);
        yield notification;
      }
      default -> {
        throw new AssertionError("Unreachable code");
      }
    };
  }

  @Override
  public void addAll(Iterator<O> keys, V value) {
    accuMap = accuMap.addAll(keys, value, this);
  }

  @Override
  public void clear() {
    accuMap = AccumulationMap.empty();
  }

}