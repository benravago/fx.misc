package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import javafx.util.Pair;

import fx.Counter;

class SuspendableYesTest {

  @Test
  void test() {
    var sy = new SuspendableYes();

    var counter = new Counter();
    sy.addListener((obs, oldVal, newVal) -> {
      assertNotEquals(oldVal, newVal);
      counter.inc();
    });

    var g = sy.suspend();

    assertEquals(1, counter.getAndReset());

    sy.suspendWhile(() -> {
    });

    assertEquals(0, counter.getAndReset());

    g.close();

    assertEquals(1, counter.getAndReset());
  }

  @Test
  void recursionTest() {
    var sy = new SuspendableYes();

    // first listener immediately suspends after resumed
    sy.addListener((ind, oldVal, newVal) -> {
      if (!newVal) {
        sy.suspend();
      }
    });

    // record changes observed by the second listener
    var changes = new ArrayList<Pair<Boolean, Boolean>>();
    EventStreams.changesOf(sy).subscribe(ch -> changes.add(new Pair<>(ch.getOldValue(), ch.getNewValue())));

    sy.suspend().close();

    for (var ch : changes) {
      assertNotEquals(ch.getKey(), ch.getValue());
    }

    for (var i = 0; i < changes.size() - 1; ++i) {
      assertEquals(
        changes.get(i).getValue(),
        changes.get(i + 1).getKey(),
        "changes[" + i + "] = " + changes.get(i) +
        " and changes[" + (i + 1) + "] = " + changes.get(i + 1) +
        " are not compatible"
      );
    }
  }

}
