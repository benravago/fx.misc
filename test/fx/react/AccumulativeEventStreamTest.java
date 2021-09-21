package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import fx.react.util.AccumulatorSize;

class AccumulativeEventStreamTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var suspendable = source.accumulative(
      () -> new LinkedList<Integer>(),
      (l, i) -> { l.addLast(i); return l; },
      l -> AccumulatorSize.fromInt(l.size()),
      l -> l.getFirst(),
      l -> { l.removeFirst(); return l; }
    );
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

}
