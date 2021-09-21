package fx.react.collection;

import fx.react.util.AccumulatorSize;

public interface Observer<E, O> {

  AccumulatorSize sizeOf(ListModificationSequence<? extends E> mods);
  O headOf(ListModificationSequence<? extends E> mods);
  <F extends E> ListModificationSequence<F> tailOf(ListModificationSequence<F> mods);
  void onChange(O change);

}
