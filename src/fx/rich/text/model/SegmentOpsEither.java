package fx.rich.text.model;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.util.Either;

class SegmentOpsEither<L, R, S> implements SegmentOps<Either<L, R>, S> {

  final SegmentOps<L, S> lOps;
  final SegmentOps<R, S> rOps;
  final BiFunction<S, S, Optional<S>> mergeStyle;

  SegmentOpsEither(SegmentOps<L, S> lOps, SegmentOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    this.lOps = lOps;
    this.rOps = rOps;
    this.mergeStyle = mergeStyle;
  }

  @Override
  public int length(Either<L, R> seg) {
    return seg.unify(lOps::length, rOps::length);
  }

  @Override
  public char charAt(Either<L, R> seg, int index) {
    return seg.unify(l -> lOps.charAt(l, index), r -> rOps.charAt(r, index));
  }

  @Override
  public String getText(Either<L, R> seg) {
    return seg.unify(lOps::getText, rOps::getText);
  }

  @Override
  public Either<L, R> subSequence(Either<L, R> seg, int start, int end) {
    return seg.map(l -> lOps.subSequence(l, start, end), r -> rOps.subSequence(r, start, end));
  }

  @Override
  public Either<L, R> subSequence(Either<L, R> seg, int start) {
    return seg.map(l -> lOps.subSequence(l, start), r -> rOps.subSequence(r, start));
  }

  @Override
  public Optional<Either<L, R>> joinSeg(Either<L, R> left, Either<L, R> right) {
    return left.unify(
      ll -> right.unify(rl -> lOps.joinSeg(ll, rl).map(Either::left), rr -> Optional.empty()),
      lr -> right.unify(rl -> Optional.empty(), rr -> rOps.joinSeg(lr, rr).map(Either::right))
    );
  }

  @Override
  public Optional<S> joinStyle(S currentStyle, S nextStyle) {
    return mergeStyle.apply(currentStyle, nextStyle);
  }

  public Either<L, R> createEmptySeg() {
    return Either.left(lOps.createEmptySeg());
  }

}