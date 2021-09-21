package fx.react.collection;

import java.util.Iterator;
import java.util.List;

import fx.util.Lists;

interface ListModificationIterable<E, M extends ListModificationLike<? extends E>> extends Iterable<M> {

  List<? extends M> getModifications();

  @Override
  default Iterator<M> iterator() {
    return Lists.readOnlyIterator(getModifications());
  }

  default int getModificationCount() {
    return getModifications().size();
  }

}
