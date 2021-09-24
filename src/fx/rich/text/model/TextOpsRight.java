package fx.rich.text.model;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.util.Either;

class TextOpsRight<L, R, S> extends SegmentOpsEither<L, R, S> implements TextOps<Either<L, R>, S> {

  TextOpsRight(SegmentOps<L, S> lOps, TextOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    super(lOps, rOps, mergeStyle);
  }

  @Override
  public Either<L, R> create(String text) {
    return Either.right(((TextOps<R,S>)rOps).create(text));
  }

}