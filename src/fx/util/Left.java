package fx.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

class Left<L, R> implements Either<L, R> {

  final L value;

  Left(L value) {
    this.value = value;
  }

  @Override
  public boolean isLeft() {
    return true;
  }

  @Override
  public boolean isRight() {
    return false;
  }

  @Override
  public L getLeft() {
    return value;
  }

  @Override
  public R getRight() {
    throw new NoSuchElementException();
  }

  @Override
  public L toLeft(Function<? super R, ? extends L> f) {
    return value;
  }

  @Override
  public R toRight(Function<? super L, ? extends R> f) {
    return f.apply(value);
  }

  @Override
  public Optional<L> asLeft() {
    return Optional.of(value);
  }

  @Override
  public Optional<R> asRight() {
    return Optional.empty();
  }

  @Override
  public void ifLeft(Consumer<? super L> f) {
    f.accept(value);
  }

  @Override
  public void ifRight(Consumer<? super R> f) {
    /* do nothing */ }

  @Override
  public void exec(Consumer<? super L> ifLeft, Consumer<? super R> ifRight) {
    ifLeft.accept(value);
  }

  @Override
  public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> f) {
    return new Left<>(f.apply(value));
  }

  @Override
  public <R2> Either<L, R2> mapRight(Function<? super R, ? extends R2> f) {
    return new Left<>(value);
  }

  @Override
  public <L2, R2> Either<L2, R2> map(Function<? super L, ? extends L2> f, Function<? super R, ? extends R2> g) {
    return new Left<>(f.apply(value));
  }

  @Override
  public <L2, R2> Either<L2, R2> flatMap(Function<? super L, Either<L2, R2>> f, Function<? super R, Either<L2, R2>> g) {
    return f.apply(value);
  }

  @Override
  public <L2> Either<L2, R> flatMapLeft(Function<? super L, Either<L2, R>> f) {
    return f.apply(value);
  }

  @Override
  public <R2> Either<L, R2> flatMapRight(Function<? super R, Either<L, R2>> f) {
    return new Left<>(value);
  }

  @Override
  public <T> T unify(Function<? super L, ? extends T> f, Function<? super R, ? extends T> g) {
    return f.apply(value);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public final boolean equals(Object other) {
    return (other instanceof Left<?, ?> that) ? Objects.equals(this.value, that.value) : false;
  }

  @Override
  public String toString() {
    return "left(" + value + ")";
  }

}