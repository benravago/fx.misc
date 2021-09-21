package fx.util.tree;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import fx.util.Either;
import fx.util.Lists;

class Leaf<T, S> extends NonEmpty<T, S> {

  final T data;
  final S summary;

  Leaf(ToSemigroup<? super T, S> semigroup, T data) {
    super(semigroup);
    this.data = data;
    this.summary = semigroup.apply(data);
  }

  @Override
  public String toString() {
    return "Leaf(" + data + ")";
  }

  @Override
  public int getDepth() {
    return 1;
  }

  @Override
  public int getLeafCount() {
    return 1;
  }

  @Override
  public List<T> asList() {
    return Collections.singletonList(data);
  }

  @Override
  T getLeaf0(int index) {
    assert index == 0;
    return data;
  }

  @Override
  NonEmpty<T, S> updateLeaf0(int index, T data) {
    assert index == 0;
    return leaf(data);
  }

  @Override
  T getData() {
    return data;
  }

  @Override
  public S getSummary() {
    return summary;
  }

  @Override
  public Optional<S> getSummaryOpt() {
    return Optional.of(summary);
  }

  @Override
  Index locateProgressively0(ToIntFunction<? super S> metric, int position) {
    assert Lists.isValidPosition(position, measure(metric));
    return new Index(0, position);
  }

  @Override
  Index locateRegressively0(ToIntFunction<? super S> metric, int position) {
    assert Lists.isValidPosition(position, measure(metric));
    return new Index(0, position);
  }

  @Override
  public <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction) {
    return reduction.apply(acc, data);
  }

  @Override
  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf) {
    assert 0 <= startLeaf;
    assert endLeaf <= 1;
    return (startLeaf < endLeaf) ? reduction.apply(acc, data) : acc;
  }

  @Override
  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction) {
    assert Lists.isValidRange(startPosition, endPosition, measure(metric));
    return rangeReduction.apply(acc, data, startPosition, endPosition);
  }

  @Override
  S getSummaryBetween0(int startLeaf, int endLeaf) {
    assert startLeaf == 0 && endLeaf == 1;
    return summary;
  }

  @Override
  S getSummaryBetween0(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary) {
    assert Lists.isNonEmptyRange(startPosition, endPosition, measure(metric))
      : "Didn't expect empty range [" + startPosition + ", " + endPosition + ")";
    return (startPosition == 0 && endPosition == measure(metric))
      ? summary : subSummary.apply(data, startPosition, endPosition);
  }

  @Override
  Either<Leaf<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> appendLte(FingerTree<T, S> right) {
    assert right.getDepth() <= this.getDepth();
    return right.caseEmpty().mapLeft(emptyRight -> this).mapRight(nonEmptyRight -> new _2t<>(this, nonEmptyRight));
  }

  @Override
  Either<Leaf<T, S>, _2t<NonEmpty<T, S>, NonEmpty<T, S>>> prependLte(FingerTree<T, S> left) {
    assert left.getDepth() <= this.getDepth();
    return left.caseEmpty().mapLeft(emptyLeft -> this).mapRight(nonEmptyLeft -> new _2t<>(nonEmptyLeft, this));
  }

  @Override
  _2t<FingerTree<T, S>, FingerTree<T, S>> split0(int beforeLeaf) {
    assert Lists.isValidPosition(beforeLeaf, 1);
    return (beforeLeaf == 0) ? new _2t<>(empty(), this) : new _2t<>(this, empty());
  }

  @Override
  Index locate0(BiFunction<? super S, Integer, Either<Integer, Integer>> navigate, int position) {
    return navigate.apply(summary, position).unify(
      inl -> new Index(0, inl),
      inr -> { throw new AssertionError("Unreachable code"); }
    );
  }

}
