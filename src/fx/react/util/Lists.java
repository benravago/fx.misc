package fx.react.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Lists extends fx.util.Lists {

  @SafeVarargs
  public static <E> List<E> concatView(List<? extends E>... lists) {
    return concatView(Arrays.asList(lists));
  }

  /**
   * Returns a list that is a concatenation of the given lists. The returned
   * list is a view of the underlying lists, without copying the elements.
   * The returned list is unmodifiable. Modifications to underlying lists
   * will be visible through the concatenation view.
   */
  public static <E> List<E> concatView(List<List<? extends E>> lists) {
    return (lists.isEmpty()) ? Collections.emptyList() : ListConcatenationView.create(lists);
  }

  @SafeVarargs
  public static <E> List<E> concat(List<? extends E>... lists) {
    return concat(Arrays.asList(lists));
  }

  /**
   * Returns a list that is a concatenation of the given lists. The returned
   * list is a view of the underlying lists, without copying the elements.
   * As opposed to {@link #concatView(List)}, the underlying lists must not
   * be modified while the returned concatenation view is in use. On the other
   * hand, this method guarantees balanced nesting if some of the underlying
   * lists are already concatenations created by this method.
   */
  public static <E> List<E> concat(List<List<? extends E>> lists) {
    return ListConcatenation.create(lists);
  }

  public static int commonPrefixLength(List<?> l, List<?> m) {
    var i = l.listIterator();
    var j = m.listIterator();
    while (i.hasNext() && j.hasNext()) {
      if (!Objects.equals(i.next(), j.next())) {
        return i.nextIndex() - 1;
      }
    }
    return i.nextIndex();
  }

  public static int commonSuffixLength(List<?> l, List<?> m) {
    var i = l.listIterator(l.size());
    var j = m.listIterator(m.size());
    while (i.hasPrevious() && j.hasPrevious()) {
      if (!Objects.equals(i.previous(), j.previous())) {
        return l.size() - i.nextIndex() - 1;
      }
    }
    return l.size() - i.nextIndex();
  }

  /**
   * Returns the lengths of common prefix and common suffix of two lists.
   * The total of the two lengths returned is not greater than either of
   * the list sizes. Prefix is prioritized: for lists [a, b, a, b], [a, b]
   * returns (2, 0); although the two lists have a common suffix of length 2,
   * the length of the second list is already included in the length of the
   * common prefix.
   */
  public static int[] commonPrefixSuffixLengths(List<?> l1, List<?> l2) {
    var n1 = l1.size();
    var n2 = l2.size();
    if (n1 == 0 || n2 == 0) {
      return new int[] {0, 0};
    }
    var pref = commonPrefixLength(l1, l2);
    if (pref == n1 || pref == n2) {
      return new int[] {pref, 0};
    }
    var suff = commonSuffixLength(l1, l2);
    return new int[] {pref, suff};
  }

}
