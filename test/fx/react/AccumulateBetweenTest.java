package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class AccumulateBetweenTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var ticks = new EventSource<Void>();
    var queued = source.queueBetween(ticks);
    var emitted = new ArrayList<Integer>();
    var sub = queued.subscribe(emitted::add);

    ticks.push(null);
    assertEquals(Arrays.asList(), emitted);

    source.push(1);
    source.push(2);
    assertEquals(Arrays.asList(), emitted);

    ticks.push(null);
    assertEquals(Arrays.asList(1, 2), emitted);

    ticks.push(null);
    assertEquals(Arrays.asList(1, 2), emitted);

    source.push(3);
    assertEquals(Arrays.asList(1, 2), emitted);
    sub.unsubscribe(); // should reset now
    sub = queued.subscribe(emitted::add);
    ticks.push(null);
    assertEquals(Arrays.asList(1, 2), emitted);
  }

}
