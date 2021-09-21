package fx.react.collection;

/**
 * Common supertype for {@link QuasiChange} and {@link ListChangeAccumulator}.
 *
 * @param <E> type of list elements
 */
public interface ListModificationSequence<E> extends ListModificationIterable<E, QuasiModification<? extends E>> {

  /**
   * May be destructive for this object. Therefore, this object should not
   * be used after the call to this method, unless stated otherwise by the
   * implementing class/interface.
   */
  QuasiChange<E> asListChange();

  /**
   * May be destructive for this object. Therefore, this object should not
   * be used after the call to this method, unless stated otherwise by the
   * implementing class/interface.
   */
  ListChangeAccumulator<E> asListChangeAccumulator();

}
