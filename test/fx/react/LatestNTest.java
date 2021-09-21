package fx.react;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LatestNTest {

  @Test
  void test() {
    var src = new EventSource<Integer>();
    var latest3 = src.latestN(3);
    var emitted = new ArrayList<List<Integer>>();
    latest3.subscribe(emitted::add);
    src.push(1);
    src.push(2);
    src.push(3);
    src.push(4);
    src.push(5);
    assertEquals(
      Arrays.asList(Arrays.asList(1),
        Arrays.asList(1, 2),
        Arrays.asList(1, 2, 3),
        Arrays.asList(2, 3, 4),
        Arrays.asList(3, 4, 5)),
      emitted);
  }

  @Test
  void testResetOnUnsubscribe() {
    var src = new EventSource<Integer>();
    var latest3 = src.latestN(3);
    var sub = latest3.pin();
    src.push(1);
    src.push(2);
    src.push(3);
    sub.unsubscribe();
    var emitted = new ArrayList<List<Integer>>();
    latest3.subscribe(emitted::add);
    src.push(4);
    assertEquals(Arrays.asList(4), emitted.get(0));
  }

}
