package fx.react.collection;

import java.util.List;

import fx.react.util.Lists;

public interface MaterializedModification<E> extends ListModificationLike<E> {

  /**
   * Doesn't create defensive copies of the passed lists.
   * Therefore, they must not be modified later.
   */
  static <E> MaterializedModification<E> create(int pos, List<? extends E> removed, List<? extends E> added) {
    return new MaterializedListModification<E>(pos, removed, added);
  }

  List<? extends E> getAdded();

  @Override
  default int getAddedSize() {
    return getAdded().size();
  }

  default MaterializedModification<E> trim() {
    var t = Lists.commonPrefixSuffixLengths(getRemoved(), getAdded());
    var pref = t[0];
    var suff = t[1];
    return (pref == 0 && suff == 0)
      ? this
      : create(
          getFrom() + pref,
          getRemoved().subList(pref, getRemovedSize() - suff),
          getAdded().subList(pref, getAddedSize() - suff)
        );
  }

}
