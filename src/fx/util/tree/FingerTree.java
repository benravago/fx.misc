package fx.util.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import fx.util.SubSequence;
import fx.util.Either;
import fx.util.Sequence;
import fx.util.Lists;

public abstract class FingerTree<T, S> {

  public record _2t<A,B>(A a, B b) {
    public <T> T map(BiFunction<? super A, ? super B, ? extends T> f) { return f.apply(a,b); }
    public void exec(BiConsumer<? super A, ? super B> f) { f.accept(a,b); }
  }

  public interface _3f<A, B, C, R> { R apply(A a, B b, C c); }
  public interface _4f<A, B, C, D, R> { R apply(A a, B b, C c, D d); }

  public static <T, S> FingerTree<T, S> empty(ToSemigroup<? super T, S> statisticsProvider) {
    return new Empty<>(statisticsProvider);
  }

  public static <T> FingerTree<T, Void> mkTree(List<? extends T> items) {
    return mkTree(items, new ToSemigroup<T, Void>() {
      @Override public Void apply(T t) { return null; }
      @Override public Void reduce(Void left, Void right) { return null; }
    });
  }

  public static <T, S> FingerTree<T, S> mkTree(List<? extends T> items, ToSemigroup<? super T, S> summaryProvider) {
    if (items.isEmpty()) {
      return new Empty<>(summaryProvider);
    }
    var trees = new ArrayList<NonEmpty<T, S>>(items.size());
    for (var item : items) {
      trees.add(new Leaf<T, S>(summaryProvider, item));
    }
    while (trees.size() > 1) {
      var n = trees.size();
      var src = 0;
      var tgt = 0;
      while (src < n) {
        if (n - src >= 5 || n - src == 3) {
          var t1 = trees.get(src++);
          var t2 = trees.get(src++);
          var t3 = trees.get(src++);
          var t = branch(t1, t2, t3);
          trees.set(tgt++, t);
        } else { // (n - i) is 4 or 2
          var t1 = trees.get(src++);
          var t2 = trees.get(src++);
          var t = branch(t1, t2);
          trees.set(tgt++, t);
        }
      }
      trees.subList(tgt, n).clear();
    }
    return trees.get(0);
  }

  static <T, S> Branch<T, S> branch(NonEmpty<T, S> left, NonEmpty<T, S> right) {
    return branch(Sequence.of(left, right));
  }

  static <T, S> Branch<T, S> branch(NonEmpty<T, S> left, NonEmpty<T, S> middle, NonEmpty<T, S> right) {
    return branch(Sequence.of(left, middle, right));
  }

  static <T, S> Branch<T, S> branch(SubSequence<NonEmpty<T, S>> children) {
    return new Branch<>(children);
  }

  static <T, S> FingerTree<T, S> concat(SubSequence<? extends FingerTree<T, S>> nodes) {
    var head = nodes.head();
    return nodes.tail().fold(head, FingerTree::join);
  }

  final ToSemigroup<? super T, S> semigroup;

  FingerTree(ToSemigroup<? super T, S> semigroup) {
    this.semigroup = semigroup;
  }

  public abstract int getDepth();
  public abstract int getLeafCount();
  public abstract Optional<S> getSummaryOpt();
  public abstract Either<FingerTree<T, S>, NonEmpty<T, S>> caseEmpty();

  public final boolean isEmpty() {
    return getDepth() == 0;
  }

  public S getSummary(S whenEmpty) {
    return getSummaryOpt().orElse(whenEmpty);
  }

  public T getLeaf(int index) {
    Lists.checkIndex(index, getLeafCount());
    return getLeaf0(index);
  }

  abstract T getLeaf0(int index);

  public _2t<T, Index> get(ToIntFunction<? super S> metric, int index) {
    return caseEmpty().unify(
      emptyTree -> { throw new IndexOutOfBoundsException("empty tree"); },
      neTree -> {
        var size = metric.applyAsInt(neTree.getSummary());
        Lists.checkIndex(index, size);
        var location = locateProgressively(metric, index);
        return new _2t<>(getLeaf(location.major), location);
      }
    );
  }

  public <E> E get(ToIntFunction<? super S> metric, int index, BiFunction<? super T, Integer, ? extends E> leafAccessor) {
    return locateProgressively(metric, index).map((major, minor) -> leafAccessor.apply(getLeaf(major), minor));
  }

  public NonEmpty<T, S> updateLeaf(int index, T data) {
    Lists.checkIndex(index, getLeafCount());
    return updateLeaf0(index, data);
  }

  abstract NonEmpty<T, S> updateLeaf0(int index, T data);

  public Index locate(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position) {
    return caseEmpty().unify(
      emptyTree -> { throw new IndexOutOfBoundsException("no leafs to locate in"); },
      neTree -> { throw new AssertionError("This method must be overridden in non-empty tree"); }
    );
  }

  public Index locateProgressively(ToIntFunction<? super S> metric, int position) {
    return caseEmpty().unify(
      emptyTree -> { throw new IndexOutOfBoundsException("no leafs to locate in"); },
      neTree -> { throw new AssertionError("This method must be overridden in non-empty tree"); }
    );
  }

  public Index locateRegressively(ToIntFunction<? super S> metric, int position) {
    return caseEmpty().unify(
      emptyTree -> { throw new IndexOutOfBoundsException("no leafs to locate in"); },
      neTree -> { throw new AssertionError("This method must be overridden in non-empty tree"); }
    );
  }

  public abstract <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction);

  public <R> R foldBetween(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf) {
    Lists.checkRange(startLeaf, endLeaf, getLeafCount());
    return (startLeaf == endLeaf) ? acc : foldBetween0(acc, reduction, startLeaf, endLeaf);
  }

  abstract <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf);

  public <R> R foldBetween(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction) {
    Lists.checkRange(startPosition, endPosition, measure(metric));
    return (startPosition == endPosition) ? acc : foldBetween0(acc, reduction, metric, startPosition, endPosition, rangeReduction);
  }

  abstract <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction);

  public Optional<S> getSummaryBetween(int startLeaf, int endLeaf) {
    Lists.checkRange(startLeaf, endLeaf, getLeafCount());
    return (startLeaf == endLeaf) ? Optional.empty() : Optional.of(getSummaryBetween0(startLeaf, endLeaf));
  }

  abstract S getSummaryBetween0(int startLeaf, int endLeaf);

  public Optional<S> getSummaryBetween(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary) {
    Lists.checkRange(startPosition, endPosition, measure(metric));
    return (startPosition == endPosition) ? Optional.empty() : Optional.of(getSummaryBetween0(metric, startPosition, endPosition, subSummary));
  }

  abstract S getSummaryBetween0(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary);

  public _2t<FingerTree<T, S>, FingerTree<T, S>> split(int beforeLeaf) {
    Lists.checkPosition(beforeLeaf, getLeafCount());
    return split0(beforeLeaf);
  }

  abstract _2t<FingerTree<T, S>, FingerTree<T, S>> split0(int beforeLeaf);

  public FingerTree<T, S> removeLeafs(int fromLeaf, int toLeaf) {
    Lists.checkRange(fromLeaf, toLeaf, getLeafCount());
    if (fromLeaf == toLeaf) {
      return this;
    } else if (fromLeaf == 0 && toLeaf == getLeafCount()) {
      return empty();
    } else {
      var left = split0(fromLeaf).a();
      var right = split0(toLeaf).b();
      return left.join(right);
    }
  }

  public FingerTree<T, S> insertLeaf(int position, T data) {
    Lists.checkPosition(position, getLeafCount());
    return split0(position).map((l, r) -> l.join(leaf(data)).join(r));
  }

  public abstract FingerTree<T, S> join(FingerTree<T, S> rightTree);

  public NonEmpty<T, S> append(T data) {
    return leaf(data).prependTree(this);
  }

  public NonEmpty<T, S> prepend(T data) {
    return leaf(data).appendTree(this);
  }

  /**
   * Returns a list view of this tree.
   * Complexity of operations on the returned list:
   * <ul>
   *   <li>{@code size()}: O(1);</li>
   *   <li>{@code get}: O(log(n));</li>
   *   <li><b>iteration</b>: O(n) in either direction,
   *     with O(log(n)) total allocations;</li>
   *   <li>{@code subList}: O(log(n));</li>
   *   <li><b>iterative {@code subList},</b> i.e. calling {@code subList}
   *     on the result of previous {@code subList}, up to n times: O(n).</li>
   * </ul>
   */
  public abstract List<T> asList();

  abstract T getData(); // valid for leafs only

  Empty<T, S> empty() {
    return new Empty<>(semigroup);
  }

  Leaf<T, S> leaf(T data) {
    return new Leaf<>(semigroup, data);
  }

  int measure(ToIntFunction<? super S> metric) {
    return getSummaryOpt().map(metric::applyAsInt).orElse(0);
  }

}
