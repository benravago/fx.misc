package fx.react.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

import fx.react.EventSource;
import fx.react.EventStream;
import fx.react.Subscription;

class StateMachineTest {

  private static class Counter {
    private int count = 0;

    void inc() {
      ++count;
    }

    int get() {
      return count;
    }

    int getAndReset() {
      int res = count;
      count = 0;
      return res;
    }
  }

  @Test
  void countDownTest() {
    EventSource<Void> src1 = new EventSource<Void>();
    EventSource<Void> src2 = new EventSource<Void>();
    EventSource<Void> reset = new EventSource<Void>();

    BiFunction<Integer, Void, Tuple2<Integer, Optional<String>>> countdown = (s,
        i) -> s == 1 ? new Tuple2<>(3, Optional.of("COUNTDOWN REACHED")) : new Tuple2<>(s - 1, Optional.empty());

    EventStream<String> countdowns = StateMachine.init(3).on(src1).transmit(countdown).on(src2).transmit(countdown)
        .on(reset).transition((s, i) -> 3).toEventStream();

    Counter counter = new Counter();
    Subscription sub = countdowns.hook(x -> counter.inc()).pin();

    src1.push(null);
    src2.push(null);
    assertEquals(0, counter.get());

    src1.push(null);
    assertEquals(1, counter.getAndReset());

    src2.push(null);
    src2.push(null);
    reset.push(null);
    assertEquals(0, counter.get());
    src2.push(null);
    assertEquals(0, counter.get());
    src1.push(null);
    assertEquals(0, counter.get());
    src2.push(null);
    assertEquals(1, counter.getAndReset());

    sub.unsubscribe();
    src1.push(null);
    src1.push(null);
    src1.push(null);
    src1.push(null);
    src1.push(null);
    src1.push(null);
    assertEquals(0, counter.get());
  }

}
