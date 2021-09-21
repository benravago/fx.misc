package fx.react.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fx.react.value.SuspendableVar;
import fx.react.value.Val;
import fx.react.value.Var;
import javafx.scene.control.IndexRange;

class ListRangeReductionTest {

  @Test
  void test() {
    LiveList<Integer> list = new LiveArrayList<>(1, 2, 4);
    Var<IndexRange> range = Var.newSimpleVar(new IndexRange(0, 0));
    Val<Integer> rangeSum = list.reduceRange(range, (a, b) -> a + b);

    assertNull(rangeSum.getValue());

    List<Integer> observed = new ArrayList<>();
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
    SuspendableList<Integer> list = new LiveArrayList<Integer>(1, 2, 3).suspendable();
    SuspendableVar<IndexRange> range = Var.newSimpleVar(new IndexRange(0, 3)).suspendable();
    Val<Integer> rangeSum = list.reduceRange(range, (a, b) -> a + b);

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
