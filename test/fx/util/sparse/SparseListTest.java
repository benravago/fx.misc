package fx.util.sparse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

@ExtendWith(SparseListTest.class)
class SparseListTest implements ParameterResolver {

  static class ListMod {
    final SparseList<Integer> list;
    final SparseListModification mod;

    ListMod(SparseList<Integer> list, SparseListModification mod) {
      this.list = list;
      this.mod = mod;
    }

    @Override
    public String toString() {
      return "TREE:\n" + list.tree + "\nMODIFICATION:\n" + mod;
    }
  }

  static Random random = new Random();

  static int random_nextInt(int min, int max) {
    var bound = max - min;
    return bound > 0 ? random.nextInt(bound) + min : 0;
  }

  static ListMod newListMod() {
      SparseList<Integer> list = SparseLists.gen();
      SparseListModification mod = SparseListModification.gen(list.size());
      return new ListMod(list, mod);
    }

  static class SparseLists {

    static SparseList<Integer> gen() {
      if (random.nextDouble() < 0.2) {
        return new SparseList<>();
      } else {
        SparseList<Integer> list = gen();
        SparseListModification.gen(list.size()).apply(list);
        return list;
      }
    }

    SparseList<Integer> generate() {
      return gen();
    }
  }

  static abstract class SparseListModification {

    static List<Integer> randomIntList() {
      int n = random.nextInt(8);
      List<Integer> list = new ArrayList<>(n);
      for (int i = 0; i < n; ++i) {
        list.add(random.nextInt());
      }
      return list;
    }

    static SparseListModification gen(int listSize) {
      int x = listSize == 0 ? random_nextInt(2, 7) : random.nextInt(8);
      switch (x) {
      case 0:
        return ElemUpdate.generate(listSize);
      case 1:
        return ElemRemoval.generate(listSize);
      case 2:
        return RangeRemoval.generate(listSize);
      case 3:
        return ElemInsert.generate(listSize);
      case 4:
        return ElemsInsert.generate(listSize);
      case 5:
        return VoidInsert.generate(listSize);
      case 6:
        return ElemsSplice.generate(listSize);
      case 7:
        return VoidSplice.generate(listSize);
      default:
        throw new AssertionError();
      }
    }

    abstract void apply(SparseList<Integer> list);
  }

  static class ElemRemoval extends SparseListModification {
    static ElemRemoval generate(int listSize) {
      return new ElemRemoval(random.nextInt(listSize));
    }

    private final int index;

    ElemRemoval(int index) {
      this.index = index;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.remove(index);
    }

    @Override
    public String toString() {
      return "ElemRemoval(" + index + ")";
    }
  }

  static class RangeRemoval extends SparseListModification {
    static RangeRemoval generate(int listSize) {
      int from = random_nextInt(0, listSize);
      int to = random_nextInt(from, listSize);
      return new RangeRemoval(from, to);
    }

    private final int from;
    private final int to;

    RangeRemoval(int from, int to) {
      this.from = from;
      this.to = to;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.remove(from, to);
    }

    @Override
    public String toString() {
      return "RangeRemoval(" + from + ", " + to + ")";
    }
  }

  static class ElemUpdate extends SparseListModification {
    static ElemUpdate generate(int listSize) {
      return new ElemUpdate(random.nextInt(listSize), random.nextInt());
    }

    private final int index;
    private final Integer elem;

    ElemUpdate(int index, Integer elem) {
      this.index = index;
      this.elem = elem;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.set(index, elem);
    }

    @Override
    public String toString() {
      return "ElemUpdate(" + index + ", " + elem + ")";
    }
  }

  static class ElemInsert extends SparseListModification {
    static ElemInsert generate(int listSize) {
      return new ElemInsert(random_nextInt(0, listSize), random.nextInt());
    }

    private final int index;
    private final Integer elem;

    ElemInsert(int index, Integer elem) {
      this.index = index;
      this.elem = elem;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.insert(index, elem);
    }

    @Override
    public String toString() {
      return "ElemInsert(" + index + ", " + elem + ")";
    }
  }

  static class ElemsInsert extends SparseListModification {
    static ElemsInsert generate(int listSize) {
      return new ElemsInsert(random_nextInt(0, listSize), randomIntList());
    }

    private final int index;
    private final Collection<Integer> elems;

    ElemsInsert(int index, Collection<Integer> elems) {
      this.index = index;
      this.elems = elems;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.insertAll(index, elems);
    }

    @Override
    public String toString() {
      return "ElemsInsert(" + index + ", " + elems + ")";
    }
  }

  static class VoidInsert extends SparseListModification {
    static VoidInsert generate(int listSize) {
      return new VoidInsert(random_nextInt(0, listSize), random.nextInt(1024));
    }

    private final int index;
    private final int length;

    VoidInsert(int index, int length) {
      this.index = index;
      this.length = length;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.insertVoid(index, length);
    }

    @Override
    public String toString() {
      return "VoidInsert(" + index + ", " + length + ")";
    }
  }

  static class ElemsSplice extends SparseListModification {
    static ElemsSplice generate(int listSize) {
      int from = random_nextInt(0, listSize);
      int to = random_nextInt(from, listSize);
      List<Integer> ints = randomIntList();
      return new ElemsSplice(from, to, ints);
    }

    private final int from;
    private final int to;
    private final Collection<Integer> elems;

    ElemsSplice(int from, int to, Collection<Integer> elems) {
      this.from = from;
      this.to = to;
      this.elems = elems;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.splice(from, to, elems);
    }

    @Override
    public String toString() {
      return "ElemSplice(" + from + ", " + to + ", " + elems + ")";
    }
  }

  static class VoidSplice extends SparseListModification {
    static VoidSplice generate(int listSize) {
      int from = random_nextInt(0, listSize);
      int to = random_nextInt(from, listSize);
      return new VoidSplice(from, to, random.nextInt(1024));
    }

    private final int from;
    private final int to;
    private final int length;

    VoidSplice(int from, int to, int length) {
      this.from = from;
      this.to = to;
      this.length = length;
    }

    @Override
    void apply(SparseList<Integer> list) {
      list.spliceByVoid(from, to, length);
    }

    @Override
    public String toString() {
      return "VoidSplice(" + from + ", " + to + ", " + length + ")";
    }
  }

  private static double log2(double arg) {
    return Math.log(arg) / Math.log(2);
  }

  private static double log3(double arg) {
    return Math.log(arg) / Math.log(3);
  }

  private static int maxTreeDepth(int segments) {
    return segments == 0 ? 0 : (int) (Math.floor(log2(segments)) + 1);
  }

  private static int minTreeDepth(int segments) {
    return segments == 0 ? 0 : (int) (Math.ceil(log3(segments)) + 1);
  }

  private static int countSegments(SparseList<?> list) {
    if (list.size() == 0) {
      return 0;
    } else {
      final int n = list.size();
      int segments = 1;
      boolean lastPresent = list.isPresent(0);
      for (int i = 1; i < n; ++i) {
        if (list.isPresent(i) != lastPresent) {
          ++segments;
          lastPresent = !lastPresent;
        }
      }
      return segments;
    }
  }

  @RepeatedTest(100)
  void leafCountEqualToSegmentCount(@Arg ListMod listMod) {
    SparseList<Integer> list = listMod.list;
    SparseListModification mod = listMod.mod;

    int segments = countSegments(list);
    int leafs = list.tree.getLeafCount();
    assumeTrue(leafs == segments);

    mod.apply(list);

    segments = countSegments(list);
    leafs = list.tree.getLeafCount();
    assertEquals(leafs, segments);
  }

  @RepeatedTest(100)
  void depthBounds(@Arg ListMod listMod) {
    SparseList<Integer> list = listMod.list;
    SparseListModification mod = listMod.mod;

    int depth = list.tree.getDepth();
    int n = countSegments(list);
    assumeTrue(depth <= maxTreeDepth(n));
    assumeTrue(depth >= minTreeDepth(n));

    mod.apply(list);

    depth = list.tree.getDepth();
    n = countSegments(list);
    assertTrue(depth <= maxTreeDepth(n));
    assertTrue(depth >= minTreeDepth(n));
  }

  @Test
  void handPickedTests() {
    SparseList<Integer> list = new SparseList<>();

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

/*
  static int seq = 1;
  static void print(String tag, ListMod lm) {
    System.out.format(
      "# %d %s\n  tree: %s\n  mods: %s\n\n",
      (seq++), tag, lm.list.tree, lm.mod);
  }
*/

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
    return newListMod();
  }
}
