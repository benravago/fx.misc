package fx.react.collection;

import fx.react.util.AccumulatorSize;

@FunctionalInterface
public interface QuasiModificationObserver<E> extends Observer<E, QuasiModification<? extends E>> {

  @Override
  default AccumulatorSize sizeOf(ListModificationSequence<? extends E> mods) {
    return AccumulatorSize.fromInt(mods.getModificationCount());
  }

  @Override
  default QuasiModification<? extends E> headOf(ListModificationSequence<? extends E> mods) {
    return mods.getModifications().get(0);
  }

  @Override
  default <F extends E> ListModificationSequence<F> tailOf(ListModificationSequence<F> mods) {
    return mods.asListChangeAccumulator().drop(1);
  }

}