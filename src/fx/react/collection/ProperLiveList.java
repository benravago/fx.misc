package fx.react.collection;

import java.util.Collections;
import java.util.List;

import fx.react.ObservableBase;
import fx.react.ProperObservable;
import fx.react.util.NotificationAccumulator;
import fx.util.Lists;

/**
 * Trait to be mixed into {@link ObservableBase} to obtain default
 * implementation of some {@link LiveList} methods and get additional
 * helper methods for implementations of <em>proper</em> {@linkplain LiveList}.
 */
public interface ProperLiveList<E> extends LiveList<E>, ProperObservable<Observer<? super E, ?>, QuasiChange<? extends E>> {

  @Override
  default NotificationAccumulator<Observer<? super E, ?>, QuasiChange<? extends E>, ?> defaultNotificationAccumulator() {
    return NotificationAccumulator.listNotifications();
  }

  default void fireModification(QuasiModification<? extends E> mod) {
    notifyObservers(mod.asListChange());
  }

  static <E> QuasiModification<E> elemReplacement(int index, E replaced) {
    return new QuasiListModification<E>(index, Collections.singletonList(replaced), 1);
  }

  default void fireElemReplacement(int index, E replaced) {
    fireModification(elemReplacement(index, replaced));
  }

  default QuasiModification<E> contentReplacement(List<E> removed) {
    return new QuasiListModification<E>(0, removed, size());
  }

  default void fireContentReplacement(List<E> removed) {
    fireModification(contentReplacement(removed));
  }

  static <E> QuasiModification<E> elemInsertion(int index) {
    return rangeInsertion(index, 1);
  }

  default void fireElemInsertion(int index) {
    fireModification(elemInsertion(index));
  }

  static <E> QuasiModification<E> rangeInsertion(int index, int size) {
    return new QuasiListModification<E>(index, Collections.emptyList(), size);
  }

  default void fireRangeInsertion(int index, int size) {
    fireModification(rangeInsertion(index, size));
  }

  static <E> QuasiModification<E> elemRemoval(int index, E removed) {
    return new QuasiListModification<E>(index, Collections.singletonList(removed), 0);
  }

  default void fireElemRemoval(int index, E removed) {
    fireModification(elemRemoval(index, removed));
  }

  static <E> QuasiModification<E> rangeRemoval(int index, List<E> removed) {
    return new QuasiListModification<E>(index, removed, 0);
  }

  default void fireRemoveRange(int index, List<E> removed) {
    fireModification(rangeRemoval(index, removed));
  }

  @Override
  default int defaultHashCode() {
    return Lists.hashCode(this);
  }

  @Override
  default boolean defaultEquals(Object o) {
    return Lists.equals(this, o);
  }

  @Override
  default String defaultToString() {
    return Lists.toString(this);
  }

}
