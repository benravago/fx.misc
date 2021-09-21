package fx.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

class SingleEntryMap<K, V> extends Dictionary<K, V> {

  final K key;
  V value;

  SingleEntryMap(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  protected Dictionary<K, V> put(K key, V value) {
    return (Objects.equals(key, this.key))
      ? new SingleEntryMap<>(key, value)
      : new MultiEntryMap<>(this.key, this.value, key, value);
  }

  @Override
  protected V get(K key) {
    return Objects.equals(key, this.key) ? value : null;
  }

  @Override
  protected Dictionary<K, V> remove(K key) {
    return (Objects.equals(key, this.key)) ? null : this;
  }

  @Override
  protected K chooseKey() {
    return key;
  }

  @Override
  protected void replaceAll(BiFunction<? super K, ? super V, ? extends V> f) {
    value = f.apply(key, value);
  }

  @Override
  protected void forEach(BiConsumer<K, V> f) {
    f.accept(key, value);
  }

  @Override
  protected int size() {
    return 1;
  }

  @Override
  protected boolean containsKey(K key) {
    return Objects.equals(key, this.key);
  }

}