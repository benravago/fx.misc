package fx.util.tree;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

class FingerTreeTest {

  /**
   * Returns a random int, with higher probability for larger numbers.
   */
  static int progressiveInt(Random rnd, int bound) {
    var d = rnd.nextDouble();
    d = d * d * d;
    var i = (int) Math.floor(d * bound);
    return bound - 1 - i;
  }

  @Test
  void testSubList() {
    final int n = 50_000;

    var arr = new Integer[n];
    for (int i = 0; i < n; ++i) {
      arr[i] = i;
    }
    var list = Arrays.asList(arr);
    var treeList = FingerTree.mkTree(list).asList();
    assertEquals(list, treeList);

    var rnd = new Random(12345);
    while (list.size() > 0) {
      var len = progressiveInt(rnd, list.size() + 1);
      var offset = rnd.nextInt(list.size() - len + 1);
      list = list.subList(offset, offset + len);
      treeList = treeList.subList(offset, offset + len);
      assertEquals(list, treeList);
    }
  }

  @Test
  void testIteration() {
    final int n = 50_000;
    final int from = 10_000;
    final int to = 40_000;

    var arr = new Integer[n];
    for (var i = 0; i < n; ++i)
      arr[i] = i;

    var list = Arrays.asList(arr);
    var treeList = FingerTree.mkTree(list).asList();

    list = list.subList(from, to);
    treeList = treeList.subList(from, to);

    var it = treeList.listIterator();

    var fwRes = new ArrayList<Integer>(treeList.size());
    while (it.hasNext()) {
      fwRes.add(it.next());
    }
    assertEquals(list, fwRes);

    var bwRes = new ArrayList<Integer>(treeList.size());
    while (it.hasPrevious()) {
      bwRes.add(it.previous());
    }
    Collections.reverse(bwRes);
    assertEquals(list, bwRes);
  }

}
