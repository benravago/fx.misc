package fx.util.tree;

import static fx.util.Either.left;
import static fx.util.Either.right;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import fx.util.Either;
import fx.util.Lists;

public abstract class NonEmpty<T, S> extends FingerTree<T, S> {

  public record _3t<A, B, C> (A a, B b, C c) {
    public <T> T map(_3f<? super A, ? super B, ? super C, ? extends T> f) { return f.apply(a, b, c); }
  }

  NonEmpty(ToSemigroup<? super T, S> semigroup) {
    super(semigroup);
  }

  @Override
  public Either<FingerTree<T, S>, NonEmpty<T, S>> caseEmpty() {
    return right(this);
  }

  public abstract S getSummary();

  @Override
  public Index locate(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position) {
    if (navigate.apply(getSummary(), position).isRight()) {
      throw new IndexOutOfBoundsException("Position " + position + " is out of bounds");
    }
    return locate0(navigate, position);
  }

  @Override
  public Index locateProgressively(ToIntFunction<? super S> metric, int position) {
    Lists.checkPosition(position, measure(metric));
    return locateProgressively0(metric, position);
  }

  @Override
  public Index locateRegressively(ToIntFunction<? super S> metric, int position) {
    Lists.checkPosition(position, measure(metric));
    return locateRegressively0(metric, position);
  }

  public _3t<FingerTree<T, S>, T, FingerTree<T, S>> splitAt(int leaf) {
    Lists.checkIndex(leaf, getLeafCount());
    return split0(leaf).map((l, r0) -> r0.split0(1).map((m, r) -> new _3t<>(l, m.getLeaf0(0), r)));
  }

  public _3t<FingerTree<T, S>, _2t<T, Integer>, FingerTree<T, S>> split(ToIntFunction<? super S> metric, int position) {
    Lists.checkPosition(position, measure(metric));
    return split((s, i) -> {
      var n = metric.applyAsInt(s);
      return i <= n ? left(i) : right(i - n);
    }, position);
  }

  public _3t<FingerTree<T, S>, _2t<T, Integer>, FingerTree<T, S>> split(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position) {
    if (navigate.apply(getSummary(), position).isRight()) {
      throw new IndexOutOfBoundsException("Position " + position + " is out of bounds");
    }
    var loc = locate0(navigate, position);
    return splitAt(loc.major).map((l, m, r) -> new _3t<>(l, new _2t<>(m, loc.minor), r));
  }

  @Override
  public NonEmpty<T, S> join(FingerTree<T, S> rightTree) {
    return appendTree(rightTree);
  }

  NonEmpty<T, S> appendTree(FingerTree<T, S> right) {
    return (this.getDepth() >= right.getDepth())
      ? appendLte(right).unify(Function.identity(), two -> two.map(FingerTree::branch))
      : ((NonEmpty<T, S>) right).prependTree(this);
  }

  NonEmpty<T, S> prependTree(FingerTree<T, S> left) {
    return (this.getDepth() >= left.getDepth())
      ? prependLte(left).unify(Function.identity(), two -> two.map(FingerTree::branch))
      : ((NonEmpty<T, S>) left).appendTree(this);
  }

  abstract Index locate0(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position);
  abstract Index locateProgressively0(ToIntFunction<? super S> metric, int position);
  abstract Index locateRegressively0(ToIntFunction<? super S> metric, int position);
  abstract Either<? extends NonEmpty<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> appendLte(FingerTree<T, S> right);
  abstract Either<? extends NonEmpty<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> prependLte(FingerTree<T, S> left);

}
