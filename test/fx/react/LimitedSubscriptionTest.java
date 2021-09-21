package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import fx.Counter;

class LimitedSubscriptionTest {

  @Test
  void testBasic() {
    var src = new EventSource<Void>();
    var counter = new Counter();
    src.subscribeFor(5, i -> counter.inc());
    for (var i = 0; i < 10; ++i) {
      src.push(null);
    }
    assertEquals(5, counter.getAndReset());
  }

  @Test
  void testOnPausedStream() {
    var src = new EventSource<Void>();
    var pausable = src.pausable();
    var counter = new Counter();
    pausable.subscribeFor(5, i -> counter.inc());
    pausable.suspendWhile(() -> {
      for (var i = 0; i < 10; ++i) {
        src.push(null);
      }
    });
    assertEquals(5, counter.getAndReset());
  }

  @Test
  void testWithAutoEmittingStream() {
    var stream = new EventStreamBase<Void>() {
      @Override
      protected Subscription observeInputs() {
        return Subscription.EMPTY;
      }
      @Override
      protected void newObserver(Consumer<? super Void> subscriber) {
        for (var i = 0; i < 10; ++i) {
          subscriber.accept(null);
        }
      }
    };
    var counter = new Counter();
    stream.subscribeFor(5, i -> counter.inc());
    assertEquals(5, counter.getAndReset());
  }

}
