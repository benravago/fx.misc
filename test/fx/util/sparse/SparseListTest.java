package fx.util.sparse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.util.Arrays;
import java.util.Collections;

@ExtendWith(SparseListTest.class)
class SparseListTest implements ParameterResolver {

  static double log2(double arg) {
    return Math.log(arg) / Math.log(2);
  }
  static double log3(double arg) {
    return Math.log(arg) / Math.log(3);
  }
  static int maxTreeDepth(int segments) {
    return segments == 0 ? 0 : (int) (Math.floor(log2(segments)) + 1);
  }
  static int minTreeDepth(int segments) {
    return segments == 0 ? 0 : (int) (Math.ceil(log3(segments)) + 1);
  }

  static int countSegments(SparseList<?> list) {
    if (list.size() == 0) {
      return 0;
    } else {
      var n = list.size();
      var segments = 1;
      var lastPresent = list.isPresent(0);
      for (var i = 1; i < n; ++i) {
        if (list.isPresent(i) != lastPresent) {
          ++segments;
          lastPresent = !lastPresent;
        }
      }
      return segments;
    }
  }

  @RepeatedTest(100)
  void leafCountEqualToSegmentCount(@Arg Sparse.ListMod listMod) {
    var list = listMod.list;
    var mod = listMod.mod;

    var segments = countSegments(list);
    var leafs = list.tree.getLeafCount();
    assumeTrue(leafs == segments);

    // print("leafCountEqualToSegmentCount",listMod);
    mod.apply(list);

    segments = countSegments(list);
    leafs = list.tree.getLeafCount();
    assertEquals(leafs, segments, listMod);
  }

  @RepeatedTest(100)
  void depthBounds(@Arg Sparse.ListMod listMod) {
    var list = listMod.list;
    var mod = listMod.mod;

    var depth = list.tree.getDepth();
    var n = countSegments(list);
    assumeTrue(depth <= maxTreeDepth(n));
    assumeTrue(depth >= minTreeDepth(n));

    // print("depthBounds",listMod);
    mod.apply(list);

    depth = list.tree.getDepth();
    n = countSegments(list);
    assertTrue(depth <= maxTreeDepth(n), listMod);
    assertTrue(depth >= minTreeDepth(n), listMod);
  }

  @Test
  void handPickedTests() {
    var list = new SparseList<Integer>();

    list.insertVoid(0, 10);
    list.insertVoid(5, 5);
    // _ _ _ _ _ _ _ _ _ _ _ _ _ _ _
    assertEquals(list.size(), 15);
    assertEquals(list.getPresentCount(), 0);
    assertEquals(list.tree.getLeafCount(), 1);

    list.splice(5, 10, Collections.emptyList());
    // _ _ _ _ _ _ _ _ _ _
    assertEquals(list.size(), 10);
    assertEquals(list.tree.getLeafCount(), 1);

    list.splice(5, 10, Arrays.asList(5, 6, 7, 8, 9));
    // _ _ _ _ _ 5 6 7 8 9
    assertEquals(list.size(), 10);
    assertEquals(list.getPresentCount(), 5);
    assertEquals(list.tree.getLeafCount(), 2);

    list.set(4, 4);
    // _ _ _ _ 4 5 6 7 8 9
    assertEquals(list.size(), 10);
    assertEquals(list.getPresentCount(), 6);
    assertEquals(list.tree.getLeafCount(), 2);

    list.splice(1, 2, Arrays.asList(1));
    // _ 1 _ _ 4 5 6 7 8 9
    assertEquals(list.size(), 10);
    assertEquals(list.getPresentCount(), 7);
    assertEquals(list.tree.getLeafCount(), 4);

    list.set(2, 2);
    // _ 1 2 _ 4 5 6 7 8 9
    assertEquals(list.size(), 10);
    assertEquals(list.getPresentCount(), 8);
    assertEquals(list.tree.getLeafCount(), 4);

    list.set(3, 3);
    // _ 1 2 3 4 5 6 7 8 9
    assertEquals(list.size(), 10);
    assertEquals(list.getPresentCount(), 9);
    assertEquals(list.tree.getLeafCount(), 2);

    assertEquals(list.collect(3, 6), Arrays.asList(3, 4, 5));
  }

  static int seq = 1;
  static void print(String tag, Sparse.ListMod lm) {
    System.out.format(
      "# %d %s\n  tree: %s\n  mods: %s\n\n",
      (seq++), tag, lm.list.tree, lm.mod);
  }

  // ParameterResolver tag
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface Arg {}

  // ParameterResolver api
  @Override
  public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
    return pc.isAnnotated(Arg.class);
  }
  @Override
  public Object resolveParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
    return Sparse.newListMod();
  }
}
