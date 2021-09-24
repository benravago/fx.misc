package fx.rich.text.model;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.util.Either;

class TextOpsLeft<L, R, S> extends SegmentOpsEither<L, R, S> implements TextOps<Either<L, R>, S> {

  TextOpsLeft(TextOps<L, S> lOps, SegmentOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    super(lOps, rOps, mergeStyle);
  }

  @Override
  public Either<L, R> create(String text) {
    return Either.left(((TextOps<L,S>)lOps).create(text));
  }

}