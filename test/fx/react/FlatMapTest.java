package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class FlatMapTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var a = new EventSource<String>();
    var b = new EventSource<String>();

    var stream = source.flatMap(i -> i == 1 ? a : b);

    var emitted = new ArrayList<String>();
    stream = stream.hook(s -> emitted.add(s));

    source.push(1);
    a.push("a");
    assertEquals(0, emitted.size()); // not yet subscribed

    var pin = stream.pin();
    source.push(1);
    a.push("a");
    assertEquals(Arrays.asList("a"), emitted);
    emitted.clear();

    source.push(2);
    a.push("A"); // ignored
    b.push("b");
    assertEquals(Arrays.asList("b"), emitted);
    emitted.clear();

    pin.unsubscribe();
    a.push("x");
    b.push("y");
    assertEquals(0, emitted.size());

    pin = stream.pin();
    a.push("x");
    b.push("y");
    assertEquals(0, emitted.size()); // source hasn't emitted yet

    source.push(1);
    a.push("x");
    b.push("y");
    assertEquals(Arrays.asList("x"), emitted);
  }

}
