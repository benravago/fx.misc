package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class RecursionTest {

  @Test
  void allowRecursionWithOneSubscriber() {
    var emitted = new ArrayList<Integer>();
    var source = new EventSource<Integer>();
    source.hook(emitted::add).subscribe(i -> {
      if (i > 0)
        source.push(i - 1);
    });
    source.push(5);
    assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), emitted);
  }

  @Test
  void preventRecursionWithTwoSubscribers() {
    assertThrows(IllegalStateException.class, () -> {
      var source = new EventSource<Integer>();

      // XXX this test depends on the implementation detail
      // that subscribers are notified in registration order
      source.subscribe(i -> {
        if (i > 0)
          source.push(i - 1);
      });
      source.pin();

      source.push(5);
    });
  }

  @Test
  void onRecurseQueueTest() {
    var source = new EventSource<Integer>();
    var stream = source.onRecurseQueue();
    var emitted1 = new ArrayList<Integer>();
    var emitted2 = new ArrayList<Integer>();

    stream.subscribe(x -> {
      emitted1.add(x);
      if (x > 0)
        source.push(x - 1);
    });
    stream.subscribe(emitted2::add);

    source.push(5);
    assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), emitted1);
    assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), emitted2);
  }

  @Test
  void onRecurseReduceTest() {
    var source = new EventSource<Integer>();
    var stream = source.onRecurseReduce((a, b) -> a + b);
    var emitted1 = new ArrayList<Integer>();
    var emitted2 = new ArrayList<Integer>();

    stream.subscribe(x -> {
      emitted1.add(x);
      if (x > 0)
        source.push(x - 1);
    });
    stream.subscribe(emitted2::add);

    source.push(5);
    assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), emitted1);
    assertEquals(15, emitted2.stream().reduce(0, (a, b) -> a + b).intValue());
  }

}
