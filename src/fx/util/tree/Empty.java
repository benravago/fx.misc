package fx.util.tree;

import static fx.util.Either.left;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import fx.util.Either;
import fx.util.Lists;

class Empty<T, S> extends FingerTree<T, S> {

  Empty(ToSemigroup<? super T, S> semigroup) {
    super(semigroup);
  }

  @Override
  public String toString() {
    return "<emtpy tree>";
  }

  @Override
  public Either<FingerTree<T, S>, NonEmpty<T, S>> caseEmpty() {
    return left(this);
  }

  @Override
  public int getDepth() {
    return 0;
  }

  @Override
  public int getLeafCount() {
    return 0;
  }

  @Override
  public FingerTree<T, S> join(FingerTree<T, S> rightTree) {
    return rightTree;
  }

  @Override
  public List<T> asList() {
    return Collections.emptyList();
  }

  @Override
  T getLeaf0(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  NonEmpty<T, S> updateLeaf0(int index, T data) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  T getData() {
    throw new NoSuchElementException();
  }

  @Override
  public Optional<S> getSummaryOpt() {
    return Optional.empty();
  }

  @Override
  public <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction) {
    return acc;
  }

  @Override
  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, int startLeaf, int endLeaf) {
    assert Lists.isValidRange(startLeaf, endLeaf, 0);
    return acc;
  }

  @Override
  <R> R foldBetween0(R acc, BiFunction<? super R, ? super T, ? extends R> reduction, ToIntFunction<? super S> metric, int startPosition, int endPosition, _4f<? super R, ? super T, Integer, Integer, ? extends R> rangeReduction) {
    assert Lists.isValidRange(startPosition, endPosition, 0);
    return acc;
  }

  @Override
  S getSummaryBetween0(int startLeaf, int endLeaf) {
    throw new AssertionError("Unreachable code");
  }

  @Override
  S getSummaryBetween0(ToIntFunction<? super S> metric, int startPosition, int endPosition, _3f<? super T, Integer, Integer, ? extends S> subSummary) {
    throw new AssertionError("Unreachable code");
  }

  @Override
  _2t<FingerTree<T, S>, FingerTree<T, S>> split0(int beforeLeaf) {
    assert beforeLeaf == 0;
    return new _2t<>(this, this);
  }

}
