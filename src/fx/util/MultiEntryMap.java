package fx.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

class MultiEntryMap<K, V> extends Dictionary<K, V> {

  final Map<K, V> entries = new HashMap<>();

  MultiEntryMap(K k1, V v1, K k2, V v2) {
    entries.put(k1, v1);
    entries.put(k2, v2);
  }

  @Override
  protected Dictionary<K, V> put(K key, V value) {
    entries.put(key, value);
    return this;
  }

  @Override
  protected V get(K key) {
    return entries.get(key);
  }

  @Override
  protected Dictionary<K, V> remove(K key) {
    entries.remove(key);
    return switch (entries.size()) {
      case 0 -> null;
      case 1 -> {
        @SuppressWarnings("unchecked")
        Entry<K, V> entry = (Entry<K, V>) entries.entrySet().toArray(new Entry<?, ?>[1])[0];
        yield new SingleEntryMap<>(entry.getKey(), entry.getValue());
      }
      default -> this;
    };
  }

  @Override
  protected K chooseKey() {
    return entries.keySet().iterator().next();
  }

  @Override
  protected void replaceAll(BiFunction<? super K, ? super V, ? extends V> f) {
    entries.replaceAll(f);
  }

  @Override
  protected void forEach(BiConsumer<K, V> f) {
    for (var entry : entries.entrySet().toArray()) {
      @SuppressWarnings("unchecked")
      Entry<K, V> e = (Entry<K, V>) entry;
      f.accept(e.getKey(), e.getValue());
    }
  }

  @Override
  protected int size() {
    return entries.size();
  }

  @Override
  protected boolean containsKey(K key) {
    return entries.containsKey(key);
  }

}
