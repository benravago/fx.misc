package fx.react.util;

import java.util.Objects;

/**
 * Base class for value-based wrappers, that is wrappers that implement
 * {@link #equals(Object)} and {@link #hashCode()} solely by comparing/hashing
 * the wrapped values.
 * @param <T> type of the wrapped value.
 */
public abstract class WrapperBase<T> {

  final T delegate;

  /**
   * @param delegate wrapped value.
   */
  protected WrapperBase(T delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("delegate cannot be null");
    }
    this.delegate = delegate;
  }

  public T getWrappedValue() {
    return delegate;
  }

  @Override
  public boolean equals(Object that) {
    return (that instanceof WrapperBase<?> wb) ? Objects.equals(wb.delegate, this.delegate) : false;
  }

  @Override
  public final int hashCode() {
    return delegate.hashCode();
  }

}