package fx.react.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.control.IndexRange;

import fx.react.value.Var;

class ListRangeReductionTest {

  @Test
  void test() {
    var list = new LiveArrayList<>(1, 2, 4);
    var range = Var.newSimpleVar(new IndexRange(0, 0));
    var rangeSum = list.reduceRange(range, (a, b) -> a + b);

    assertNull(rangeSum.getValue());

    var observed = new ArrayList<Integer>();
    rangeSum.values().subscribe(sum -> {
      observed.add(sum);
      if (sum == null) {
        range.setValue(new IndexRange(0, 2));
      } else if (sum == 3) {
        list.addAll(1, Arrays.asList(8, 16));
      } else if (sum == 9) {
        range.setValue(new IndexRange(2, 4));
      }
    });

    assertEquals(Arrays.asList(null, 3, 9, 18), observed);
  }

  /**
   * Tests the case when both list and range have been modified and range
   * change notification arrived first.
   */
  @Test
  void testLateListNotifications() {
    var list = new LiveArrayList<Integer>(1, 2, 3).suspendable();
    var range = Var.newSimpleVar(new IndexRange(0, 3)).suspendable();
    var rangeSum = list.reduceRange(range, (a, b) -> a + b);

    list.suspendWhile(() -> {
      range.suspendWhile(() -> {
        list.addAll(4, 5, 6);
        range.setValue(new IndexRange(3, 6));
      });
    });
    assertEquals(15, rangeSum.getValue().intValue());

    // most importantly, this test tests that no IndexOutOfBoundsException is thrown
  }

}
