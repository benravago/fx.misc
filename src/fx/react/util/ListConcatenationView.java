package fx.react.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

class ListConcatenationView<E> extends AbstractList<E> {

  static <E> List<E> create(List<List<? extends E>> lists) {
    return concatView(lists, true);
  }

  static <E> List<E> concatView(List<List<? extends E>> lists, boolean makeUnmodifiable) {
    var len = lists.size();
    if (len < 1) {
      throw new AssertionError("Supposedly unreachable code");
    } else if (len == 1) {
      var list = lists.get(0);
      if (makeUnmodifiable) {
        return Collections.unmodifiableList(list);
      } else {
        @SuppressWarnings("unchecked")
        List<E> lst = (List<E>) list;
        return lst;
      }
    } else {
      int mid = len / 2;
      return new ListConcatenationView<>(concatView(lists.subList(0, mid), false), concatView(lists.subList(mid, len), false));
    }
  }

  final List<? extends E> first;
  final List<? extends E> second;

  ListConcatenationView(List<? extends E> first, List<? extends E> second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public E get(int index) {
    return (index < first.size()) ? first.get(index) : second.get(index - first.size());
  }

  @Override
  public int size() {
    return first.size() + second.size();
  }

}