package fx.react;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import fx.react.collection.Change;
import fx.react.collection.ModifiedList;
import fx.react.collection.LiveList;
import fx.react.util.FxTimer;
import fx.react.util.Timer;
import fx.util.Either;

public class EventStreams {

  /**
   * Returns an event stream that never emits any value.
   */
  @SuppressWarnings("unchecked")
  public static <T> EventStream<T> never() {
    return (EventStream<T>) NeverStream.instance();
  }

  /**
   * Creates an event stream that emits an impulse on every invalidation
   * of the given observable.
   */
  public static EventStream<Void> invalidationsOf(Observable observable) {
    return new EventStreamBase<Void>() {
      @Override
      protected Subscription observeInputs() {
        InvalidationListener listener = obs -> emit(null);
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
      }
    };
  }

  /**
   * Creates an event stream that emits the given observable immediately for
   * every subscriber and re-emits it on every subsequent invalidation of the
   * observable.
   */
  public static <O extends Observable> EventStream<O> repeatOnInvalidation(O observable) {
    return new EventStreamBase<O>() {
      @Override
      protected Subscription observeInputs() {
        InvalidationListener listener = obs -> emit(observable);
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
      }
      @Override
      protected void newObserver(Consumer<? super O> subscriber) {
        subscriber.accept(observable);
      }
    };
  }

  /**
   * Creates an event stream that emits the value of the given
   * {@code ObservableValue} immediately for every subscriber and then on
   * every change.
   */
  public static <T> EventStream<T> valuesOf(ObservableValue<T> observable) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        ChangeListener<T> listener = (obs, old, val) -> emit(val);
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
      }
      @Override
      protected void newObserver(Consumer<? super T> subscriber) {
        subscriber.accept(observable.getValue());
      }
    };
  }

  public static <T> EventStream<T> nonNullValuesOf(ObservableValue<T> observable) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        ChangeListener<T> listener = (obs, old, val) -> {
          if (val != null) {
            emit(val);
          }
        };
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
      }
      @Override
      protected void newObserver(Consumer<? super T> subscriber) {
        var val = observable.getValue();
        if (val != null) {
          subscriber.accept(val);
        }
      }
    };
  }

  public static <T> EventStream<Change<T>> changesOf(ObservableValue<T> observable) {
    return new EventStreamBase<Change<T>>() {
      @Override
      protected Subscription observeInputs() {
        ChangeListener<T> listener = (obs, old, val) -> emit(new Change<>(old, val));
        observable.addListener(listener);
        return () -> observable.removeListener(listener);
      }
    };
  }

  /**
   * @see LiveList#changesOf(ObservableList)
   */
  public static <T> EventStream<ListChangeListener.Change<? extends T>> changesOf(ObservableList<T> list) {
    return new EventStreamBase<ListChangeListener.Change<? extends T>>() {
      @Override
      protected Subscription observeInputs() {
        ListChangeListener<T> listener = c -> emit(c);
        list.addListener(listener);
        return () -> list.removeListener(listener);
      }
    };
  }

  /**
   * Use only when the subscriber does not cause {@code list} modification
   * of the underlying list.
   */
  public static <T> EventStream<ModifiedList<? extends T>> simpleChangesOf(ObservableList<T> list) {
    return new EventStreamBase<ModifiedList<? extends T>>() {
      @Override
      protected Subscription observeInputs() {
        return LiveList.observeChanges(list, c -> {
          for (var mod : c) {
            emit(mod);
          }
        });
      }
    };
  }

  public static <T> EventStream<SetChangeListener.Change<? extends T>> changesOf(ObservableSet<T> set) {
    return new EventStreamBase<SetChangeListener.Change<? extends T>>() {
      @Override
      protected Subscription observeInputs() {
        SetChangeListener<T> listener = c -> emit(c);
        set.addListener(listener);
        return () -> set.removeListener(listener);
      }
    };
  }

  public static <K, V> EventStream<MapChangeListener.Change<? extends K, ? extends V>> changesOf(ObservableMap<K, V> map) {
    return new EventStreamBase<MapChangeListener.Change<? extends K, ? extends V>>() {
      @Override
      protected Subscription observeInputs() {
        MapChangeListener<K, V> listener = c -> emit(c);
        map.addListener(listener);
        return () -> map.removeListener(listener);
      }
    };
  }

  public static <C extends Collection<?> & Observable> EventStream<Integer> sizeOf(C collection) {
    return create(() -> collection.size(), collection);
  }

  public static EventStream<Integer> sizeOf(ObservableMap<?, ?> map) {
    return create(() -> map.size(), map);
  }

  private static <T> EventStream<T> create(Supplier<? extends T> computeValue, Observable... dependencies) {
    return new EventStreamBase<T>() {
      T previousValue;

      @Override
      protected Subscription observeInputs() {
        InvalidationListener listener = obs -> {
          var value = computeValue.get();
          if (value != previousValue) {
            previousValue = value;
            emit(value);
          }
        };
        for (var dep : dependencies) {
          dep.addListener(listener);
        }
        previousValue = computeValue.get();
        return () -> {
          for (Observable dep : dependencies) {
            dep.removeListener(listener);
          }
        };
      }
      @Override
      protected void newObserver(Consumer<? super T> subscriber) {
        subscriber.accept(previousValue);
      }
    };
  }

  public static <T extends Event> EventStream<T> eventsOf(Node node, EventType<T> eventType) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        EventHandler<T> handler = this::emit;
        node.addEventHandler(eventType, handler);
        return () -> node.removeEventHandler(eventType, handler);
      }
    };
  }

  public static <T extends Event> EventStream<T> eventsOf(Scene scene, EventType<T> eventType) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        EventHandler<T> handler = this::emit;
        scene.addEventHandler(eventType, handler);
        return () -> scene.removeEventHandler(eventType, handler);
      }
    };
  }

  public static <T extends Event> EventStream<T> eventsOf(MenuItem menuItem, EventType<T> eventType) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        EventHandler<T> handler = this::emit;
        menuItem.addEventHandler(eventType, handler);
        return () -> menuItem.removeEventHandler(eventType, handler);
      }
    };
  }

  public static <T extends Event> EventStream<T> eventsOf(Window window, EventType<T> eventType) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        EventHandler<T> handler = this::emit;
        window.addEventHandler(eventType, handler);
        return () -> window.removeEventHandler(eventType, handler);
      }
    };
  }

  /**
   * Returns an event stream that emits periodic <i>ticks</i>. The first tick
   * is emitted after {@code interval} amount of time has passed.
   * The returned stream may only be used on the JavaFX application thread.
   *
   * <p>As with all lazily bound streams, ticks are emitted only when there
   * is at least one subscriber to the returned stream. This means that to
   * release associated resources, it suffices to unsubscribe from the
   * returned stream.
   */
  public static EventStream<?> ticks(Duration interval) {
    return new EventStreamBase<Void>() {
      final Timer timer = FxTimer.createPeriodic(interval, () -> emit(null));
      @Override
      protected Subscription observeInputs() {
        timer.restart();
        return timer::stop;
      }
    };
  }

  /**
   * Returns an event stream that emits periodic <i>ticks</i>. The first tick
   * is emitted at time 0.
   * The returned stream may only be used on the JavaFX application thread.
   *
   * <p>As with all lazily bound streams, ticks are emitted only when there
   * is at least one subscriber to the returned stream. This means that to
   * release associated resources, it suffices to unsubscribe from the
   * returned stream.
   */
  public static EventStream<?> ticks0(Duration interval) {
    return new EventStreamBase<Void>() {
      final Timer timer = FxTimer.createPeriodic0(interval, () -> emit(null));
      @Override
      protected Subscription observeInputs() {
        timer.restart();
        return timer::stop;
      }
    };
  }

  /**
   * Returns an event stream that emits periodic <i>ticks</i> on the given
   * {@code eventThreadExecutor}. The returned stream may only be used from
   * that executor's thread.
   *
   * <p>As with all lazily bound streams, ticks are emitted only when there
   * is at least one subscriber to the returned stream. This means that to
   * release associated resources, it suffices to unsubscribe from the
   * returned stream.
   *
   * @param scheduler scheduler used to schedule periodic emissions.
   * @param eventThreadExecutor single-thread executor used to emit the ticks.
   */
  public static EventStream<?> ticks(Duration interval, ScheduledExecutorService scheduler, Executor eventThreadExecutor) {
    return new EventStreamBase<Void>() {
      final Timer timer = ScheduledExecutorServiceTimer.createPeriodic(interval, () -> emit(null), scheduler, eventThreadExecutor);
      @Override
      protected Subscription observeInputs() {
        timer.restart();
        return timer::stop;
      }
    };
  }

  /**
   * Returns a {@link #ticks(Duration)} EventStream whose timer restarts whenever
   * impulse emits an event.
   * @param interval - the amount of time that passes until this stream emits its next tick
   * @param impulse - the EventStream that resets this EventStream's internal timer
   */
  public static EventStream<?> restartableTicks(Duration interval, EventStream<?> impulse) {
    return new EventStreamBase<Void>() {
      final Timer timer = FxTimer.createPeriodic(interval, () -> emit(null));
      @Override
      protected Subscription observeInputs() {
        timer.restart();
        return Subscription.multi(impulse.subscribe(x -> timer.restart()), timer::stop);
      }
    };
  }

  /**
   * Returns a {@link #ticks0(Duration)} EventStream whose timer restarts whenever
   * impulse emits an event. Note: since {@link #ticks0(Duration)} is used, restarting
   * the timer will make the returned EventStream immediately emit a new tick.
   * @param interval - the amount of time that passes until this stream emits its next tick
   * @param impulse - the EventStream that resets this EventStream's internal timer
   */
  public static EventStream<?> restartableTicks0(Duration interval, EventStream<?> impulse) {
    return new EventStreamBase<Void>() {
      final Timer timer = FxTimer.createPeriodic0(interval, () -> emit(null));
      @Override
      protected Subscription observeInputs() {
        timer.restart();
        return Subscription.multi(impulse.subscribe(x -> timer.restart()), timer::stop);
      }
    };
  }

  /**
   * Returns an event stream that emits a timestamp of the current frame in
   * nanoseconds on every frame. The timestamp has the same meaning as the
   * argument of the {@link AnimationTimer#handle(long)} method.
   */
  public static EventStream<Long> animationTicks() {
    return new EventStreamBase<Long>() {
      final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
          emit(now);
        }
      };
      @Override
      protected Subscription observeInputs() {
        timer.start();
        return timer::stop;
      }
    };
  }

  /**
   * Returns a stream that, on each animation frame, emits the duration
   * elapsed since the previous animation frame, in nanoseconds.
   */
  public static EventStream<Long> animationFrames() {
    return animationTicks()
      .accumulate(new Di<>(0L, -1L), (state, now) -> {
        var last = state.b();
        return new Di<>(last == -1L ? 0L : now - last, now);
      })
      .map(t -> t.a());
  }

  /**
   * Returns an event stream that emits all the events emitted from any of
   * the {@code inputs}. The event type of the returned stream is the nearest
   * common super-type of all the {@code inputs}.
   *
   * @see EventStream#or(EventStream)
   */
  @SafeVarargs
  public static <T> EventStream<T> merge(EventStream<? extends T>... inputs) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        return Subscription.multi(i -> i.subscribe(this::emit), inputs);
      }
    };
  }

  /**
   * Returns an event stream that emits all the events emitted from any of
   * the event streams in the given observable set. When an event stream is
   * added to the set, the returned stream will start emitting its events.
   * When an event stream is removed from the set, its events will no longer
   * be emitted from the returned stream.
   */
  public static <T> EventStream<T> merge(ObservableSet<? extends EventStream<T>> set) {
    return new EventStreamBase<T>() {
      @Override
      protected Subscription observeInputs() {
        return Subscription.dynamic(set, s -> s.subscribe(this::emit));
      }
    };
  }

  /**
   * A more general version of {@link #merge(ObservableSet)} for a set of
   * arbitrary element type and a function to obtain an event stream from
   * the element.
   * @param set observable set of elements
   * @param f function to obtain an event stream from an element
   */
  public static <T, U> EventStream<U> merge(ObservableSet<? extends T> set, Function<? super T, ? extends EventStream<U>> f) {
    return new EventStreamBase<U>() {
      @Override
      protected Subscription observeInputs() {
        return Subscription.dynamic(set, t -> f.apply(t).subscribe(this::emit));
      }
    };
  }

  public static <L, R> Di<EventStream<L>, EventStream<R>> fork(EventStream<? extends Either<L, R>> stream) {
    return new Di<>(stream.filterMap(Either::asLeft), stream.filterMap(Either::asRight));
  }

  public static <A, B> EventStream<Di<A, B>> zip(EventStream<A> _A, EventStream<B> _B) {
    return new EventStreamBase<Di<A, B>>() {
      Pocket<A> a = new ExclusivePocket<>();
      Pocket<B> b = new ExclusivePocket<>();

      @Override
      protected Subscription observeInputs() {
        a.clear(); b.clear();
        return Subscription.multi(
          _A.subscribe(_a -> { a.set(_a); tryEmit(); }),
          _B.subscribe(_b -> { b.set(_b); tryEmit(); })
        );
      }
      void tryEmit() {
        if (a.hasValue() && b.hasValue()) {
          emit(new Di<>(a.getAndClear(), b.getAndClear()));
        }
      }
    };
  }

  public record Di<A,B>(A a, B b) {}

  public static <A, B> EventStream<Di<A, B>> combine(EventStream<A> _A, EventStream<B> _B) {
    return new EventStreamBase<Di<A, B>>() {
      Pocket<A> a = new Pocket<>();
      Pocket<B> b = new Pocket<>();

      @Override
      protected Subscription observeInputs() {
        a.clear(); b.clear();
        return Subscription.multi(
          _A.subscribe(_a -> { a.set(_a); tryEmit(); }),
          _B.subscribe(_b -> { b.set(_b); tryEmit(); })
        );
      }
      void tryEmit() {
        if (a.hasValue() && b.hasValue()) {
          emit(new Di<>(a.get(), b.get()));
        }
      }
    };
  }

  public record Tri<A,B,C>(A a, B b, C c) {}

  public static <A, B, C> EventStream<Tri<A, B, C>> combine(EventStream<A> _A, EventStream<B> _B, EventStream<C> _C) {
    return new EventStreamBase<Tri<A, B, C>>() {
      Pocket<A> a = new Pocket<>();
      Pocket<B> b = new Pocket<>();
      Pocket<C> c = new Pocket<>();

      @Override
      protected Subscription observeInputs() {
        a.clear(); b.clear(); c.clear();
        return Subscription.multi(
          _A.subscribe(_a -> { a.set(_a); tryEmit(); }),
          _B.subscribe(_b -> { b.set(_b); tryEmit(); }),
          _C.subscribe(_c -> { c.set(_c); tryEmit(); })
        );
      }
      void tryEmit() {
        if (a.hasValue() && b.hasValue() && c.hasValue()) {
          emit(new Tri<>(a.get(), b.get(), c.get()));
        }
      }
    };
  }

  static class Pocket<T> {

    boolean hasValue = false;
    T value = null;

    boolean hasValue() {
      return hasValue;
    }

    void set(T value) {
      this.value = value;
      hasValue = true;
    }

    T get() {
      return value;
    }

    void clear() {
      hasValue = false;
      value = null;
    }

    T getAndClear() {
      T res = get();
      clear();
      return res;
    }
  }

  static class ExclusivePocket<T> extends Pocket<T> {
    @Override
    void set(T a) {
      if (hasValue()) {
        throw new IllegalStateException("Value arrived out of order: " + a);
      } else {
        super.set(a);
      }
    };
  }

}
