package fx.util.sparse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

class Sparse {

  static ListMod newListMod() {
    var list = newList();
    var mod = newMod(list.size());
    return new ListMod(list, mod);
  }

  static SparseList<Integer> newList() {
    if (rnd.nextDouble() < 0.2) {
      return new SparseList<>();
    } else {
      var list = newList();
      newMod(list.size()).apply(list);
      return list;
    }
  }

  static Mod newMod(int listSize) {
    var x = listSize == 0 ? random_nextInt(2, 7) : rnd.nextInt(8);
    return switch (x) {
      case 0 -> ElemUpdate.generate(listSize);
      case 1 -> ElemRemoval.generate(listSize);
      case 2 -> RangeRemoval.generate(listSize);
      case 3 -> ElemInsert.generate(listSize);
      case 4 -> ElemsInsert.generate(listSize);
      case 5 -> VoidInsert.generate(listSize);
      case 6 -> ElemsSplice.generate(listSize);
      case 7 -> VoidSplice.generate(listSize);
      default -> { throw new AssertionError(); }
    };
  }

  final static Random rnd = new Random();

  static int nextInt() {
    return rnd.nextInt();
  }
  static int nextInt(int bound) {
    return rnd.nextInt(bound);
  }
  static int random_nextInt(int min, int max) {
    var bound = max - min;
    return bound > 0 ? rnd.nextInt(bound) + min : 0;
  }

  static class ListMod implements Supplier<String> {
    SparseList<Integer> list;
    Mod mod;

    ListMod(SparseList<Integer> list, Mod mod) {
      this.list = list;
      this.mod = mod;
    }

    @Override
    public String get() { return toString(); }

    @Override
    public String toString() {
      return "TREE:\n" + list.tree + "\nMODIFICATION:\n" + mod;
    }
  }

  static List<Integer> randomIntList() {
    var n = rnd.nextInt(8);
    var list = new ArrayList<Integer>(n);
    for (var i = 0; i < n; ++i) {
      list.add(rnd.nextInt());
    }
    return list;
  }

  static abstract class Mod {
    abstract void apply(SparseList<Integer> list);
  }

  static class ElemRemoval extends Mod {
    static ElemRemoval generate(int listSize) {
      return new ElemRemoval(rnd.nextInt(listSize));
    }

    final int index;

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

  static class RangeRemoval extends Mod {
    static RangeRemoval generate(int listSize) {
      var from = random_nextInt(0, listSize);
      var to = random_nextInt(from, listSize);
      return new RangeRemoval(from, to);
    }

    final int from;
    final int to;

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

  static class ElemUpdate extends Mod {
    static ElemUpdate generate(int listSize) {
      return new ElemUpdate(rnd.nextInt(listSize), rnd.nextInt());
    }

    final int index;
    final Integer elem;

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

  static class ElemInsert extends Mod {
    static ElemInsert generate(int listSize) {
      return new ElemInsert(random_nextInt(0, listSize), rnd.nextInt());
    }

    final int index;
    final Integer elem;

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

  static class ElemsInsert extends Mod {
    static ElemsInsert generate(int listSize) {
      return new ElemsInsert(random_nextInt(0, listSize), randomIntList());
    }

    final int index;
    final Collection<Integer> elems;

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

  static class VoidInsert extends Mod {
    static VoidInsert generate(int listSize) {
      return new VoidInsert(random_nextInt(0, listSize), rnd.nextInt(1024));
    }

    final int index;
    final int length;

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

  static class ElemsSplice extends Mod {
    static ElemsSplice generate(int listSize) {
      var from = random_nextInt(0, listSize);
      var to = random_nextInt(from, listSize);
      var ints = randomIntList();
      return new ElemsSplice(from, to, ints);
    }

    final int from;
    final int to;
    final Collection<Integer> elems;

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

  static class VoidSplice extends Mod {
    static VoidSplice generate(int listSize) {
      var from = random_nextInt(0, listSize);
      var to = random_nextInt(from, listSize);
      return new VoidSplice(from, to, rnd.nextInt(1024));
    }

    final int from;
    final int to;
    final int length;

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

}
