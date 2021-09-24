package fx.rich.text.model;

import java.util.Optional;

import fx.util.Either;

class SegmentOpsStyled<LSeg, RSeg, LStyle, RStyle> implements SegmentOps<Either<LSeg, RSeg>, Either<LStyle, RStyle>> {

  final SegmentOps<LSeg, LStyle> lOps;
  final SegmentOps<RSeg, RStyle> rOps;

  SegmentOpsStyled(SegmentOps<LSeg, LStyle> lOps, SegmentOps<RSeg, RStyle> rOps) {
    this.lOps = lOps;
    this.rOps = rOps;
  }

  @Override
  public int length(Either<LSeg, RSeg> seg) {
    return seg.unify(lOps::length, rOps::length);
  }

  @Override
  public char charAt(Either<LSeg, RSeg> seg, int index) {
    return seg.unify(lSeg -> lOps.charAt(lSeg, index), rSeg -> rOps.charAt(rSeg, index));
  }

  @Override
  public String getText(Either<LSeg, RSeg> seg) {
    return seg.unify(lOps::getText, rOps::getText);
  }

  @Override
  public Either<LSeg, RSeg> subSequence(Either<LSeg, RSeg> seg, int start, int end) {
    return seg.map(lSeg -> lOps.subSequence(lSeg, start, end), rSeg -> rOps.subSequence(rSeg, start, end));
  }

  @Override
  public Either<LSeg, RSeg> subSequence(Either<LSeg, RSeg> seg, int start) {
    return seg.map(lSeg -> lOps.subSequence(lSeg, start), rSeg -> rOps.subSequence(rSeg, start));
  }

  @Override
  public Optional<Either<LSeg, RSeg>> joinSeg(Either<LSeg, RSeg> left, Either<LSeg, RSeg> right) {
    return left.unify(
      ll -> right.unify(rl -> lOps.joinSeg(ll, rl).map(Either::left), rr -> Optional.empty()),
      lr -> right.unify(rl -> Optional.empty(), rr -> rOps.joinSeg(lr, rr).map(Either::right))
    );
  }

  @Override
  public Optional<Either<LStyle, RStyle>> joinStyle(Either<LStyle, RStyle> left, Either<LStyle, RStyle> right) {
    return left.unify(
      ll -> right.unify(rl -> lOps.joinStyle(ll, rl).map(Either::left), rr -> Optional.empty()),
      lr -> right.unify(rl -> Optional.empty(), rr -> rOps.joinStyle(lr, rr).map(Either::right))
    );
  }

  public Either<LSeg, RSeg> createEmptySeg() {
    return Either.left(lOps.createEmptySeg());
  }

}