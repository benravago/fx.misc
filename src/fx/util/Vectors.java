package fx.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class Vectors {

  public static <T> T get(Vector<T> listHelper, int index) {
    Lists.checkIndex(index, size(listHelper)); // always throws for listHelper == null
    return listHelper.get(index);
  }

  public static <T> Vector<T> add(Vector<T> listHelper, T elem) {
    return (listHelper == null) ? new SingleItemList<>(elem) : listHelper.add(elem);
  }

  public static <T> Vector<T> remove(Vector<T> listHelper, T elem) {
    return (listHelper == null) ? listHelper : listHelper.remove(elem);
  }

  public static <T> void forEach(Vector<T> listHelper, Consumer<? super T> f) {
    if (listHelper != null) {
      listHelper.forEach(f);
    }
  }

  public static <T> void forEachBetween(Vector<T> listHelper, int from, int to, Consumer<? super T> f) {
    Lists.checkRange(from, to, size(listHelper));
    if (from < to) {
      listHelper.forEachBetween(from, to, f);
    }
  }

  public static <T> Iterator<T> iterator(Vector<T> listHelper) {
    return (listHelper != null) ? listHelper.iterator() : Collections.emptyIterator();
  }

  public static <T> Iterator<T> iterator(Vector<T> listHelper, int from, int to) {
    Lists.checkRange(from, to, size(listHelper));
    return (from < to) ? listHelper.iterator(from, to) : Collections.emptyIterator();
  }

  public static <T> Optional<T> reduce(Vector<T> listHelper, BinaryOperator<T> f) {
    return (listHelper == null) ? Optional.empty() : listHelper.reduce(f);
  }

  public static <T, U> U reduce(Vector<T> listHelper, U unit, BiFunction<U, T, U> f) {
    return (listHelper == null) ? unit : listHelper.reduce(unit, f);
  }

  public static <T> T[] toArray(Vector<T> listHelper, IntFunction<T[]> allocator) {
    return (listHelper == null) ? allocator.apply(0) : listHelper.toArray(allocator);
  }

  public static <T> boolean isEmpty(Vector<T> listHelper) {
    return listHelper == null;
  }

  public static <T> int size(Vector<T> listHelper) {
    return (listHelper == null) ? 0 : listHelper.size();
  }

}
