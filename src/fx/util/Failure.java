package fx.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

class Failure<T> extends Left<Throwable, T> implements Try<T> {

  Failure(Throwable thrown) {
    super(thrown);
  }

  @Override
  public T getOrElse(T fallback) {
    return fallback;
  }

  @Override
  public T getOrElse(Supplier<T> fallback) {
    return fallback.get();
  }

  @Override
  public Try<T> orElse(Try<T> fallback) {
    return fallback;
  }

  @Override
  public Try<T> orElse(Supplier<Try<T>> fallback) {
    return fallback.get();
  }

  @Override
  public Try<T> orElseTry(Callable<? extends T> fallback) {
    return Try.tryGet(fallback);
  }

  @Override
  public Try<T> recover(Function<Throwable, Optional<T>> f) {
    var recovered = f.apply(getFailure());
    return (recovered.isPresent()) ? new Success<>(recovered.get()) : this;
  }

  @Override
  public <U> Try<U> map(Function<? super T, ? extends U> f) {
    return new Failure<>(getFailure());
  }

  @Override
  public <U> Try<U> flatMap(Function<? super T, Try<U>> f) {
    return new Failure<>(getFailure());
  }

  @Override
  public String toString() {
    return "failure(" + getFailure() + ")";
  }

}