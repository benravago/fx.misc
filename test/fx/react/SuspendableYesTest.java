package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.util.Pair;

class SuspendableYesTest {

  @Test
  void test() {
    SuspendableYes sy = new SuspendableYes();

    Counter counter = new Counter();
    sy.addListener((obs, oldVal, newVal) -> {
      assertNotEquals(oldVal, newVal);
      counter.inc();
    });

    Guard g = sy.suspend();

    assertEquals(1, counter.getAndReset());

    sy.suspendWhile(() -> {
    });

    assertEquals(0, counter.getAndReset());

    g.close();

    assertEquals(1, counter.getAndReset());
  }

  @Test
  void recursionTest() {
    SuspendableYes sy = new SuspendableYes();

    // first listener immediately suspends after resumed
    sy.addListener((ind, oldVal, newVal) -> {
      if (!newVal) {
        sy.suspend();
      }
    });

    // record changes observed by the second listener
    List<Pair<Boolean, Boolean>> changes = new ArrayList<>();
    EventStreams.changesOf(sy).subscribe(ch -> changes.add(new Pair<>(ch.getOldValue(), ch.getNewValue())));

    sy.suspend().close();

    for (var ch : changes) {
      assertNotEquals(ch.getKey(), ch.getValue());
    }

    for (int i = 0; i < changes.size() - 1; ++i) {
      assertEquals(changes.get(i).getValue(), changes.get(i + 1).getKey(), "changes[" + i + "] = " + changes.get(i) + " and changes["
          + (i + 1) + "] = " + changes.get(i + 1) + " are not compatible"

      );
    }
  }
}
