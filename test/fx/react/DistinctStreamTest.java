package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DistinctStreamTest {

  <T> void testDistinct(List<T> input, List<T> expectedOutput) {
    var source = new EventSource<T>();
    var distinct = new DistinctStream<>(source);
    var distinctCollector = new ArrayList<>();
    distinct.subscribe(distinctCollector::add);
    input.forEach(source::push);
    assertEquals(expectedOutput, distinctCollector);
  }

  @Test
  void testStream() {
    testDistinct(Arrays.asList(0, 1, 1, 0), Arrays.asList(0, 1, 0));
  }

  @Test
  void testStreamWithNulls() {
    testDistinct(Arrays.asList(null, null, 1, null, null), Arrays.asList(null, 1, null));
  }

}
