package fx.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

class VoidSequence<T> extends Sequence<T> {

  private VoidSequence() {}
  private static class Singleton {
    static final VoidSequence<?> INSTANCE = new VoidSequence<>();
  }

  @SuppressWarnings("unchecked")
  static <T> VoidSequence<T> instance() {
    return (VoidSequence<T>) Singleton.INSTANCE;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public T head() {
    throw new NoSuchElementException();
  }

  @Override
  public Sequence<T> tail() {
    throw new NoSuchElementException();
  }

  @Override
  public <U> Sequence<U> map(Function<? super T, ? extends U> f) {
    return instance();
  }

  @Override
  public Iterator<T> iterator() {
    return Collections.emptyIterator();
  }

  @Override
  public <R> R fold(R acc, BiFunction<? super R, ? super T, ? extends R> reduction) {
    return acc;
  }

  @Override
  public <R> Optional<R> mapReduce(Function<? super T, ? extends R> map, BinaryOperator<R> reduce) {
    return Optional.empty();
  }

}