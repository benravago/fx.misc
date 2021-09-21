package fx.react;

import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;

/**
 * Interface for objects that can be temporarily <em>suspended</em>, where the
 * definition of "suspended" depends on the context. For example, when an
 * {@link Observable} is {@linkplain Suspendable}, it means that its
 * notification delivery can be suspended temporarily. In that case, what
 * notifications are delivered when notifications are resumed depends on the
 * concrete implementation. For example, notifications produced while suspended
 * may be queued, accumulated, or ignored completely.
 */
public interface Suspendable {

  /**
   * Suspends this suspendable object.
   *
   * <p>In case of suspendable {@link Observable},
   * suspends notification delivery for this observable object.
   * Notifications produced while suspended may be queued for later delivery,
   * accumulated into a single cumulative notification, or discarded
   * completely, depending on the concrete implementation.
   *
   * @return a {@linkplain Guard} instance that can be released to end
   * suspension. In case of suspended notifications, releasing the returned
   * {@linkplain Guard} will trigger delivery of queued or accumulated
   * notifications, if any.
   *
   * <p>The returned {@code Guard} is {@code AutoCloseable}, which makes it
   * convenient to use in try-with-resources.
   */
  Guard suspend();

  /**
   * Runs the given computation while suspended.
   *
   * <p>Equivalent to
   * <pre>
   * try(Guard g = suspend()) {
   *     r.run();
   * }
   * </pre>
   */
  default void suspendWhile(Runnable r) {
    try (Guard g = suspend()) {
      r.run();
    }
  };

  /**
   * Runs the given computation while suspended.
   *
   * The code
   *
   * <pre>
   * T t = this.suspendWhile(f);
   * </pre>
   *
   * is equivalent to
   *
   * <pre>
   * T t;
   * try(Guard g = suspend()) {
   *     t = f.get();
   * }
   * </pre>
   *
   * @return the result produced by the given supplier {@code f}.
   */
  default <U> U suspendWhile(Supplier<U> f) {
    try (Guard g = suspend()) {
      return f.get();
    }
  }

  /**
   * Arranges to suspend this {@linkplain Suspendable} whenever
   * {@code condition} is {@code true}.
   *
   * @return A {@linkplain Subscription} that can be used to stop observing
   * {@code condition} and stop suspending this {@linkplain Suspendable} based
   * on {@code condition}. If at the time of unsubscribing the returned
   * {@linkplain Subscription} this {@linkplain Suspendable} was suspended due
   * to {@code condition} being {@code true}, it will be resumed.
   */
  default Subscription suspendWhen(ObservableValue<Boolean> condition) {
    return new Subscription() {
      Guard suspensionGuard = null;
      final Subscription sub = EventStreams
        .valuesOf(condition).subscribe(this::suspendSource).and(this::resumeSource);

      @Override
      public void unsubscribe() {
        sub.unsubscribe();
      }

      void suspendSource(boolean suspend) {
        if (suspend) {
          suspendSource();
        } else {
          resumeSource();
        }
      }
      void suspendSource() {
        if (suspensionGuard == null) {
          suspensionGuard = Suspendable.this.suspend();
        }
      }
      void resumeSource() {
        if (suspensionGuard != null) {
          var toClose = suspensionGuard;
          suspensionGuard = null;
          toClose.close();
        }
      }
    };
  }

  /**
   * Returns a {@linkplain Suspendable} that combines all the given
   * {@linkplain Suspendable}s into one. When that combined
   * {@linkplain Suspendable} is suspended, all participating
   * {@linkplain Suspendable}s are suspended, in the given order. When
   * resumed, all participating {@linkplain Suspendable}s are resumed, in
   * reverse order.
   *
   */
  static Suspendable combine(Suspendable... suspendables) {
    return switch (suspendables.length) {
      case 0 -> { throw new IllegalArgumentException("Must invoke with at least 1 argument"); }
      case 1 -> suspendables[0];
      case 2 -> new BiSuspendable(suspendables[0], suspendables[1]);
      default -> new MultiSuspendable(suspendables);
    };
  }

}
