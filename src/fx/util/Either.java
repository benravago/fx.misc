package fx.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Either<L, R> {

  static <L, R> Either<L, R> left(L l) {
    return new Left<>(l);
  }

  static <L, R> Either<L, R> right(R r) {
    return new Right<>(r);
  }

  static <L, R> Either<L, R> leftOrNull(Optional<L> l) {
    return leftOrDefault(l, null);
  }

  static <L, R> Either<L, R> rightOrNull(Optional<R> r) {
    return rightOrDefault(r, null);
  }

  static <L, R> Either<L, R> leftOrDefault(Optional<L> l, R r) {
    return l.isPresent() ? left(l.get()) : right(r);
  }

  static <L, R> Either<L, R> rightOrDefault(Optional<R> r, L l) {
    return r.isPresent() ? right(r.get()) : left(l);
  }

  boolean isLeft();

  boolean isRight();

  L getLeft();

  R getRight();

  L toLeft(Function<? super R, ? extends L> f);

  R toRight(Function<? super L, ? extends R> f);

  Optional<L> asLeft();

  Optional<R> asRight();

  void ifLeft(Consumer<? super L> f);

  void ifRight(Consumer<? super R> f);

  void exec(Consumer<? super L> ifLeft, Consumer<? super R> ifRight);

  <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> f);

  <R2> Either<L, R2> mapRight(Function<? super R, ? extends R2> f);

  <L2, R2> Either<L2, R2> map(Function<? super L, ? extends L2> f, Function<? super R, ? extends R2> g);

  <L2, R2> Either<L2, R2> flatMap(Function<? super L, Either<L2, R2>> f, Function<? super R, Either<L2, R2>> g);

  <L2> Either<L2, R> flatMapLeft(Function<? super L, Either<L2, R>> f);

  <R2> Either<L, R2> flatMapRight(Function<? super R, Either<L, R2>> f);

  <T> T unify(Function<? super L, ? extends T> f, Function<? super R, ? extends T> g);

}