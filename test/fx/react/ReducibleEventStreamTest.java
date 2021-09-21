package fx.react;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReducibleEventStreamTest {

  @Test
  void test() {
    EventSource<Integer> source = new EventSource<>();
    SuspendableEventStream<Integer> suspendable = source.reducible((a, b) -> a + b);
    List<Integer> emitted = new ArrayList<>();
    suspendable.subscribe(emitted::add);

    source.push(1);
    suspendable.suspendWhile(() -> {
      source.push(2);
      source.push(3);
    });
    source.push(4);

    assertEquals(Arrays.asList(1, 5, 4), emitted);
  }

}
