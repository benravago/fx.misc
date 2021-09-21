package fx.util;

import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Dictionaries {

  public static <K, V> Dictionary<K, V> put(Dictionary<K, V> mapHelper, K key, V value) {
    return (mapHelper == null) ? new SingleEntryMap<>(key, value) : mapHelper.put(key, value);
  }

  public static <K, V> V get(Dictionary<K, V> mapHelper, K key) {
    return (mapHelper == null) ? null : mapHelper.get(key);
  }

  public static <K, V> Dictionary<K, V> remove(Dictionary<K, V> mapHelper, K key) {
    return (mapHelper == null) ? mapHelper : mapHelper.remove(key);
  }

  public static <K, V> K chooseKey(Dictionary<K, V> mapHelper) {
    if (mapHelper == null) {
      throw new NoSuchElementException("empty map");
    } else {
      return mapHelper.chooseKey();
    }
  }

  public static <K, V> void replaceAll(Dictionary<K, V> mapHelper, BiFunction<? super K, ? super V, ? extends V> f) {
    if (mapHelper != null) {
      mapHelper.replaceAll(f);
    }
  }

  public static <K, V> void forEach(Dictionary<K, V> mapHelper, BiConsumer<K, V> f) {
    if (mapHelper != null) {
      mapHelper.forEach(f);
    }
  }

  public static <K, V> boolean isEmpty(Dictionary<K, V> mapHelper) {
    return mapHelper == null;
  }

  public static <K, V> int size(Dictionary<K, V> mapHelper) {
    return (mapHelper == null) ? 0 : mapHelper.size();
  }

  public static <K> boolean containsKey(Dictionary<K, ?> mapHelper, K key) {
    return (mapHelper == null) ? false : mapHelper.containsKey(key);
  }

}
