package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class PausableEventStreamTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var suspendable = source.pausable();
    var emitted = new ArrayList<Integer>();
    suspendable.subscribe(emitted::add);

    source.push(1);
    suspendable.suspendWhile(() -> {
      source.push(2);
      source.push(3);
    });
    source.push(4);

    assertEquals(Arrays.asList(1, 2, 3, 4), emitted);
  }

  @Test
  void testRecursion() {
    var source = new EventSource<Integer>();
    var suspendable = source.pausable();
    var emitted = new ArrayList<>();
    suspendable.subscribe(emitted::add);
    suspendable.subscribe(i -> {
      if (i == 1) {
        source.push(3);
      }
    });
    suspendable.suspendWhile(() -> {
      source.push(1);
      source.push(2);
    });

    assertEquals(Arrays.asList(1, 2, 3), emitted);
  }

}
