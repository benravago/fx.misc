package fx.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Lists {

  public static <E> int hashCode(List<E> list) {
    var hashCode = 1;
    for (var e : list) {
      hashCode = 31 * hashCode + Objects.hashCode(e);
    }
    return hashCode;
  }

  public static boolean equals(List<?> list, Object o) {
    if (o == list) {
      return true;
    }
    if (o instanceof List<?> that) {
      if (list.size() != that.size()) {
        return false;
      }
      var i = list.iterator();
      var j = that.iterator();
      while (i.hasNext()) {
        if (!Objects.equals(i.next(), j.next())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public static String toString(List<?> list) {
    var res = new StringBuilder();
    res.append('[');
    var it = list.iterator();
    while (it.hasNext()) {
      res.append(it.next());
      if (it.hasNext()) {
        res.append(", ");
      }
    }
    res.append(']');
    return res.toString();
  }

  public static <E> Iterator<E> readOnlyIterator(Collection<? extends E> col) {
    return new Iterator<E>() {
      final Iterator<? extends E> delegate = col.iterator();
      @Override
      public boolean hasNext() {
        return delegate.hasNext();
      }
      @Override
      public E next() {
        return delegate.next();
      }
    };
  }

  public static boolean isValidIndex(int index, int size) {
    return isValidIndex(0, index, size);
  }

  public static boolean isValidIndex(int min, int index, int max) {
    return min <= index && index < max;
  }

  public static void checkIndex(int index, int size) {
    checkIndex(0, index, size);
  }

  public static void checkIndex(int min, int index, int max) {
    if (!isValidIndex(min, index, max)) {
      throw new IndexOutOfBoundsException(index + " not in [" + min + ", " + max + ")");
    }
  }

  public static boolean isValidPosition(int position, int size) {
    return isValidPosition(0, position, size);
  }

  public static boolean isValidPosition(int min, int position, int max) {
    return min <= position && position <= max;
  }

  public static void checkPosition(int position, int size) {
    checkPosition(0, position, size);
  }

  public static void checkPosition(int min, int position, int max) {
    if (!isValidPosition(min, position, max)) {
      throw new IndexOutOfBoundsException(position + " not in [" + min + ", " + max + "]");
    }
  }

  public static boolean isValidRange(int from, int to, int size) {
    return isValidRange(0, from, to, size);
  }

  public static boolean isValidRange(int min, int from, int to, int max) {
    return min <= from && from <= to && to <= max;
  }

  public static void checkRange(int from, int to, int size) {
    checkRange(0, from, to, size);
  }

  public static void checkRange(int min, int from, int to, int max) {
    if (!isValidRange(min, from, to, max)) {
      throw new IndexOutOfBoundsException("[" + from + ", " + to + ") is not a valid range within " + "[" + min + ", " + max + ")");
    }
  }

  public static boolean isNonEmptyRange(int from, int to, int size) {
    return isNonEmptyRange(0, from, to, size);
  }

  public static boolean isNonEmptyRange(int min, int from, int to, int max) {
    return min <= from && from < to && to <= max;
  }

  public static boolean isProperRange(int from, int to, int size) {
    return isProperRange(0, from, to, size);
  }

  public static boolean isProperRange(int min, int from, int to, int max) {
    return isValidRange(min, from, to, max) && (min < from || to < max);
  }

  public static boolean isProperNonEmptyRange(int from, int to, int size) {
    return isProperNonEmptyRange(0, from, to, size);
  }

  public static boolean isProperNonEmptyRange(int min, int from, int to, int max) {
    return isNonEmptyRange(min, from, to, max) && (min < from || to < max);
  }

  public static boolean isStrictlyInsideRange(int from, int to, int size) {
    return isStrictlyInsideRange(0, from, to, size);
  }

  public static boolean isStrictlyInsideRange(int min, int from, int to, int max) {
    return min < from && from <= to && to < max;
  }

  public static boolean isStrictlyInsideNonEmptyRange(int from, int to, int size) {
    return isStrictlyInsideNonEmptyRange(0, from, to, size);
  }

  public static boolean isStrictlyInsideNonEmptyRange(int min, int from, int to, int max) {
    return min < from && from < to && to < max;
  }

  public static <E, F> List<F> mappedView(List<? extends E> source, Function<? super E, ? extends F> f) {
    return new AbstractList<F>() {
      @Override
      public F get(int index) {
        return f.apply(source.get(index));
      }
      @Override
      public int size() {
        return source.size();
      }
    };
  }

}
