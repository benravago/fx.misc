package fx.react.value;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.util.Pair;

import fx.Counter;

class ListenerInvocationTest {

  @Test
  void test() {
    var invalidations = new Counter();
    var observedChange = Var.<Pair<Integer, Integer>>newSimpleVar(null);

    var src = Var.newSimpleVar(1);
    var squared = src.map(i -> i * i);
    squared.addListener(obs -> invalidations.inc());

    assertEquals(0, invalidations.get());

    src.setValue(2);
    assertEquals(1, invalidations.getAndReset());

    src.setValue(3);
    assertEquals(0, invalidations.getAndReset());

    squared.addListener((obs, oldVal, newVal) -> {
      observedChange.setValue(new Pair<>(oldVal, newVal));
    });

    assertNull(observedChange.getValue());

    src.setValue(4);
    assertEquals(1, invalidations.getAndReset());
    assertEquals(new Pair<>(9, 16), observedChange.getValue());
    observedChange.setValue(null);

    src.setValue(-4);
    assertEquals(1, invalidations.getAndReset());
    assertNull(observedChange.getValue());
  }

}
