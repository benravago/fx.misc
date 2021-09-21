package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class LimitedSubscriptionTest {

  @Test
  void testBasic() {
    EventSource<Void> src = new EventSource<>();
    Counter counter = new Counter();
    src.subscribeFor(5, i -> counter.inc());
    for (int i = 0; i < 10; ++i) {
      src.push(null);
    }
    assertEquals(5, counter.getAndReset());
  }

  @Test
  void testOnPausedStream() {
    EventSource<Void> src = new EventSource<>();
    SuspendableEventStream<Void> pausable = src.pausable();
    Counter counter = new Counter();
    pausable.subscribeFor(5, i -> counter.inc());
    pausable.suspendWhile(() -> {
      for (int i = 0; i < 10; ++i) {
        src.push(null);
      }
    });
    assertEquals(5, counter.getAndReset());
  }

  @Test
  void testWithAutoEmittingStream() {
    EventStream<Void> stream = new EventStreamBase<Void>() {
      @Override
      protected Subscription observeInputs() {
        return Subscription.EMPTY;
      }

      @Override
      protected void newObserver(Consumer<? super Void> subscriber) {
        for (int i = 0; i < 10; ++i) {
          subscriber.accept(null);
        }
      }
    };
    Counter counter = new Counter();
    stream.subscribeFor(5, i -> counter.inc());
    assertEquals(5, counter.getAndReset());
  }
}
