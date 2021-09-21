package fx.react.collection;

import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;

import fx.react.EventStream;
import fx.react.EventStreamBase;
import fx.react.Observable;
import fx.react.Subscription;
import fx.react.value.Val;

/**
 * Adds additional methods to {@link ObservableList}.
 *
 * @param <E> type of list elements
 */
public interface LiveList<E> extends ObservableList<E>, Observable<Observer<? super E, ?>> {

  default void addQuasiChangeObserver(QuasiChangeObserver<? super E> observer) {
    addObserver(observer);
  }

  default void removeQuasiChangeObserver(QuasiChangeObserver<? super E> observer) {
    removeObserver(observer);
  }

  default void addQuasiModificationObserver(QuasiModificationObserver<? super E> observer) {
    addObserver(observer);
  }

  default void removeQuasiModificationObserver(QuasiModificationObserver<? super E> observer) {
    removeObserver(observer);
  }

  default void addChangeObserver(Consumer<? super ListChange<? extends E>> observer) {
    addQuasiChangeObserver(new ChangeObserverWrapper<>(this, observer));
  }

  default void removeChangeObserver(Consumer<? super ListChange<? extends E>> observer) {
    removeQuasiChangeObserver(new ChangeObserverWrapper<>(this, observer));
  }

  default void addModificationObserver(Consumer<? super ModifiedList<? extends E>> observer) {
    addQuasiModificationObserver(new ModificationObserverWrapper<>(this, observer));
  }

  default void removeModificationObserver(Consumer<? super ModifiedList<? extends E>> observer) {
    removeQuasiModificationObserver(new ModificationObserverWrapper<>(this, observer));
  }

  default Subscription observeQuasiChanges(QuasiChangeObserver<? super E> observer) {
    addQuasiChangeObserver(observer);
    return () -> removeQuasiChangeObserver(observer);
  }

  default Subscription observeQuasiModifications(QuasiModificationObserver<? super E> observer) {
    addQuasiModificationObserver(observer);
    return () -> removeQuasiModificationObserver(observer);
  }

  default Subscription observeChanges(Consumer<? super ListChange<? extends E>> observer) {
    addChangeObserver(observer);
    return () -> removeChangeObserver(observer);
  }

  default Subscription observeModifications(Consumer<? super ModifiedList<? extends E>> observer) {
    addModificationObserver(observer);
    return () -> removeModificationObserver(observer);
  }

  @Override
  default void addListener(ListChangeListener<? super E> listener) {
    addQuasiChangeObserver(new ChangeListenerWrapper<>(this, listener));
  }

  @Override
  default void removeListener(ListChangeListener<? super E> listener) {
    removeQuasiChangeObserver(new ChangeListenerWrapper<>(this, listener));
  }

  @Override
  default void addListener(InvalidationListener listener) {
    addQuasiChangeObserver(new InvalidationListenerWrapper<>(this, listener));
  }

  @Override
  default void removeListener(InvalidationListener listener) {
    removeQuasiChangeObserver(new InvalidationListenerWrapper<>(this, listener));
  }

  default Subscription pin() {
    return observeQuasiChanges(qc -> {});
  }

  default Val<Integer> sizeProperty() {
    return sizeOf(this);
  }

  default <F> LiveList<F> map(Function<? super E, ? extends F> f) {
    return map(this, f);
  }

  default <F> LiveList<F> mapDynamic(ObservableValue<? extends Function<? super E, ? extends F>> f) {
    return mapDynamic(this, f);
  }

  default SuspendableList<E> suspendable() {
    return suspendable(this);
  }

  default MemoizedList<E> memoize() {
    return memoize(this);
  }

  default Val<E> reduce(BinaryOperator<E> reduction) {
    return reduce(this, reduction);
  }

  default Val<E> reduceRange(ObservableValue<IndexRange> range, BinaryOperator<E> reduction) {
    return reduceRange(this, range, reduction);
  }

  default <T> Val<T> collapse(Function<? super List<E>, ? extends T> f) {
    return collapse(this, f);
  }

  default <T> Val<T> collapseDynamic(ObservableValue<? extends Function<? super List<E>, ? extends T>> f) {
    return collapseDynamic(this, f);
  }

  default EventStream<QuasiChange<? extends E>> quasiChanges() {
    return new EventStreamBase<QuasiChange<? extends E>>() {
      @Override
      protected Subscription observeInputs() {
        return observeQuasiChanges(this::emit);
      }
    };
  }

  default EventStream<ListChange<? extends E>> changes() {
    return quasiChanges().map(qc -> QuasiChange.instantiate(qc, this));
  }

  default EventStream<QuasiModification<? extends E>> quasiModifications() {
    return new EventStreamBase<QuasiModification<? extends E>>() {
      @Override
      protected Subscription observeInputs() {
        return observeQuasiModifications(this::emit);
      }
    };
  }

  default EventStream<ModifiedList<? extends E>> modifications() {
    return quasiModifications().map(qm -> QuasiModification.instantiate(qm, this));
  }

  static <E> Subscription observeQuasiChanges(ObservableList<? extends E> list, QuasiChangeObserver<? super E> observer) {
    if (list instanceof LiveList<? extends E> lst) {
      return lst.observeQuasiChanges(observer);
    } else {
      ListChangeListener<E> listener = ch -> {
        var change = QuasiChange.from(ch);
        observer.onChange(change);
      };
      list.addListener(listener);
      return () -> list.removeListener(listener);
    }
  }

  static <E> Subscription observeChanges(ObservableList<E> list, Consumer<? super ListChange<? extends E>> observer) {
    return observeQuasiChanges(list, qc -> observer.accept(QuasiChange.instantiate(qc, list)));
  }

  static <E> EventStream<QuasiChange<? extends E>> quasiChangesOf(ObservableList<E> list) {
    if (list instanceof LiveList<E> lst) {
      return lst.quasiChanges();
    } else {
      return new EventStreamBase<QuasiChange<? extends E>>() {
        @Override
        protected Subscription observeInputs() {
          return LiveList.<E>observeQuasiChanges(list, this::emit);
        }
      };
    }
  }

  static <E> EventStream<ListChange<? extends E>> changesOf(ObservableList<E> list) {
    return quasiChangesOf(list).map(qc -> QuasiChange.instantiate(qc, list));
  }

  static Val<Integer> sizeOf(ObservableList<?> list) {
    return Val.create(() -> list.size(), list);
  }

  static <E, F> LiveList<F> map(ObservableList<? extends E> list, Function<? super E, ? extends F> f) {
    return new MappedList<>(list, f);
  }

  static <E, F> LiveList<F> mapDynamic(ObservableList<? extends E> list, ObservableValue<? extends Function<? super E, ? extends F>> f) {
    return new DynamicallyMappedList<>(list, f);
  }

  static <E> SuspendableList<E> suspendable(ObservableList<E> list) {
    return (list instanceof SuspendableList<E> sl) ? sl : new SuspendableListWrapper<>(list);
  }

  static <E> MemoizedList<E> memoize(ObservableList<E> list) {
    return (list instanceof MemoizedList<E> ml) ? ml : new ListMemoization<>(list);
  }

  static <E> Val<E> reduce(ObservableList<E> list, BinaryOperator<E> reduction) {
    return new ListReduction<>(list, reduction);
  }

  static <E> Val<E> reduceRange(ObservableList<E> list, ObservableValue<IndexRange> range, BinaryOperator<E> reduction) {
    return new ListReductionRange<>(list, range, reduction);
  }

  static <E, T> Val<T> collapse(ObservableList<? extends E> list, Function<? super List<E>, ? extends T> f) {
    return Val.create(() -> f.apply(Collections.unmodifiableList(list)), list);
  }

  static <E, T> Val<T> collapseDynamic(ObservableList<? extends E> list, ObservableValue<? extends Function<? super List<E>, ? extends T>> f) {
    return Val.create(() -> f.getValue().apply(Collections.unmodifiableList(list)), list, f);
  }

  /**
   * Returns a {@linkplain LiveList} view of the given
   * {@linkplain ObservableValue} {@code obs}. The returned list will have
   * size 1 when the given observable value is not {@code null} and size 0
   * otherwise.
   */
  static <E> LiveList<E> wrapVal(ObservableValue<E> obs) {
    return new ValAsList<>(obs);
  }

}
