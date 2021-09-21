package fx.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class Dictionary<K, V> {

  abstract Dictionary<K, V> put(K key, V value);
  abstract V get(K key);
  abstract Dictionary<K, V> remove(K key);
  abstract K chooseKey();
  abstract void replaceAll(BiFunction<? super K, ? super V, ? extends V> f);
  abstract boolean containsKey(K key);
  abstract void forEach(BiConsumer<K, V> f);
  abstract int size();

}
