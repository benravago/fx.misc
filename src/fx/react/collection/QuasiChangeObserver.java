package fx.react.collection;

import java.util.NoSuchElementException;

import fx.react.util.AccumulatorSize;

@FunctionalInterface
public interface QuasiChangeObserver<E> extends Observer<E, QuasiChange<? extends E>> {

  @Override
  default AccumulatorSize sizeOf(ListModificationSequence<? extends E> mods) {
    return AccumulatorSize.ONE;
  }

  @Override
  default QuasiChange<? extends E> headOf(ListModificationSequence<? extends E> mods) {
    return mods.asListChange();
  }

  @Override
  default <F extends E> ListModificationSequence<F> tailOf(ListModificationSequence<F> mods) {
    throw new NoSuchElementException();
  }

}