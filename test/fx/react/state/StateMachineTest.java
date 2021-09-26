package fx.react.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.react.EventSource;
import fx.state.StateMachine;
import fx.state.machine.Transmission;

import fx.Counter;

class StateMachineTest {

  @Test
  void countDownTest() {
    var src1 = new EventSource<Void>();
    var src2 = new EventSource<Void>();
    var reset = new EventSource<Void>();

    var countdown = (BiFunction<Integer, Void, Transmission<Integer, Optional<String>>>)
      (s, i) -> s == 1 ? new Transmission<>(3, Optional.of("COUNTDOWN REACHED"))
                       : new Transmission<>(s - 1, Optional.empty());

    var countdowns = StateMachine
      .init(3)
      .on(src1).transmit(countdown)
      .on(src2).transmit(countdown)
      .on(reset).transition((s, i) -> 3)
      .toEventStream();

    var counter = new Counter();
    var sub = countdowns.hook(x -> counter.inc()).pin();

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
