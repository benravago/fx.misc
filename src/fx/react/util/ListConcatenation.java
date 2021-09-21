package fx.react.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import fx.util.Lists;
import fx.util.tree.FingerTree;
import fx.util.tree.ToSemigroup;

class ListConcatenation<E> extends AbstractList<E> {

  static final ToSemigroup<List<?>, Integer> LIST_SIZE_MONOID = new ToSemigroup<List<?>, Integer>() {
    @Override
    public Integer apply(List<?> t) {
      return t.size();
    }
    @Override
    public Integer reduce(Integer left, Integer right) {
      return left + right;
    }
  };

  static <E> List<E> create(List<List<? extends E>> lists) {
    return lists.stream()
      .filter(l -> !l.isEmpty())
      .map(l -> {
        @SuppressWarnings("unchecked") // cast safe because l is unmodifiable
        List<E> lst = (List<E>) l;
        return (lst instanceof ListConcatenation<E> lc)
          ? lc.ft : FingerTree.mkTree(Collections.singletonList(lst), LIST_SIZE_MONOID);
       })
      .reduce(FingerTree::join)
      .<List<E>>map(ListConcatenation<E>::new)
      .orElse(Collections.emptyList());
  }

  final FingerTree<List<E>, Integer> ft;

  ListConcatenation(FingerTree<List<E>, Integer> ft) {
    this.ft = ft;
  }

  @Override
  public E get(int index) {
    return ft.get(Integer::intValue, index, List::get);
  }

  @Override
  public int size() {
    return ft.getSummary(0);
  }

  @Override
  public List<E> subList(int from, int to) {
    Lists.checkRange(from, to, size());
    return trim(to).drop(from);
  }

  ListConcatenation<E> trim(int limit) {
    return ft.caseEmpty().<ListConcatenation<E>>unify(
      emptyTree -> this,
      neTree -> neTree
        .split(Integer::intValue, limit)
        .map((l, m, r) -> {
          var t = m.map((lst, i) -> i == 0 ? l : l.append(lst.subList(0, i)));
          return new ListConcatenation<>(t);
        })
      );
  }

  ListConcatenation<E> drop(int n) {
    return ft.caseEmpty().<ListConcatenation<E>>unify(
      emptyTree -> this,
      neTree -> neTree
        .split(Integer::intValue, n)
        .map((l, m, r) -> {
          var t = m.map((lst, i) -> i == lst.size() ? r : r.prepend(lst.subList(i, lst.size())));
          return new ListConcatenation<>(t);
        })
      );
  }

}
