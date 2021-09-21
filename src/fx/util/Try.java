package fx.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Try<T> extends Either<Throwable, T> {

  static <T> Try<T> success(T value) {
    return new Success<>(value);
  }

  static <T> Try<T> failure(Throwable thrown) {
    return new Failure<>(thrown);
  }

  static <T> Try<T> tryGet(Callable<? extends T> f) {
    try {
      return success(f.call());
    } catch (Throwable t) {
      return failure(t);
    }
  }

  default boolean isSuccess() {
    return isRight();
  }

  default boolean isFailure() {
    return isLeft();
  }

  default T get() {
    return getRight();
  }

  default Throwable getFailure() {
    return getLeft();
  }

  default Optional<T> toOptional() {
    return asRight();
  }

  default void ifSuccess(Consumer<? super T> f) {
    ifRight(f);
  }

  default void ifFailure(Consumer<? super Throwable> f) {
    ifLeft(f);
  }

  T getOrElse(T fallback);

  T getOrElse(Supplier<T> fallback);

  Try<T> orElse(Try<T> fallback);

  Try<T> orElse(Supplier<Try<T>> fallback);

  Try<T> orElseTry(Callable<? extends T> fallback);

  Try<T> recover(Function<Throwable, Optional<T>> f);

  <U> Try<U> map(Function<? super T, ? extends U> f);

  <U> Try<U> flatMap(Function<? super T, Try<U>> f);

}