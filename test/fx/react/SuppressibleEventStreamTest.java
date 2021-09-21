package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class SuppressibleEventStreamTest {

  @Test
  void test() {
    var source = new EventSource<Integer>();
    var suspendable = source.suppressible();
    var emitted = new ArrayList<Integer>();
    suspendable.subscribe(emitted::add);

    source.push(1);
    suspendable.suspendWhile(() -> {
      source.push(2);
      source.push(3);
    });
    source.push(4);

    assertEquals(Arrays.asList(1, 4), emitted);
  }

}
