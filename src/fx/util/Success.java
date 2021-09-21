package fx.util;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

class Success<T> extends Right<Throwable, T> implements Try<T> {

  Success(T value) {
    super(value);
  }

  @Override
  public T getOrElse(T fallback) {
    return getRight();
  }

  @Override
  public T getOrElse(Supplier<T> fallback) {
    return getRight();
  }

  @Override
  public Try<T> orElse(Try<T> fallback) {
    return this;
  }

  @Override
  public Try<T> orElse(Supplier<Try<T>> fallback) {
    return this;
  }

  @Override
  public Try<T> orElseTry(Callable<? extends T> fallback) {
    return this;
  }

  @Override
  public Try<T> recover(Function<Throwable, Optional<T>> f) {
    return this;
  }

  @Override
  public <U> Try<U> map(Function<? super T, ? extends U> f) {
    return new Success<>(f.apply(get()));
  }

  @Override
  public <U> Try<U> flatMap(Function<? super T, Try<U>> f) {
    return f.apply(get());
  }

  @Override
  public String toString() {
    return "success(" + get() + ")";
  }

}