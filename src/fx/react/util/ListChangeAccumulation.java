package fx.react.util;

import fx.react.collection.ListChangeAccumulator;
import fx.react.collection.ListModificationSequence;
import fx.react.collection.QuasiChange;

interface ListChangeAccumulation<E> extends AccumulationFacility<QuasiChange<? extends E>, ListModificationSequence<E>> {

  @Override
  default ListModificationSequence<E> initialAccumulator(QuasiChange<? extends E> value) {
    return QuasiChange.safeCast(value);
  }

  @Override
  default ListChangeAccumulator<E> reduce(ListModificationSequence<E> accum, QuasiChange<? extends E> value) {
    return accum.asListChangeAccumulator().add(value);
  }

}