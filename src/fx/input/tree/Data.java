package fx.input.tree;

import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

class Data<K, V> extends NonEmpty<K, V> {

  final K key;
  final V value;

  Data(Ops<K, V> ops, K key, V value) {
    super(ops);
    this.key = key;
    this.value = value;
  }

  @Override
  K getPrefix() {
    return key;
  }

  @Override
  public Stream<Entry<K, V>> entries() {
    return Stream.of(new SimpleEntry<>(key, value));
  }

  @Override
  Data<K, V> collapse() {
    return this;
  }

  @Override
  NonEmpty<K, V> insertInside(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine) {
    assert ops.isPrefixOf(this.key, key);
    return new Data<>(this.ops, this.key, combine.apply(this.value, ops.promote(value, key, this.key)));
  }

  NonEmpty<K, V> insertInside(NonEmpty<K, V> tree, BiFunction<? super V, ? super V, ? extends V> combine) {
    var d = tree.collapse();
    return insertInside(d.key, d.value, combine);
  }

  Data<K, V> promote(K key) {
    assert ops.isPrefixOf(key, this.key);
    return new Data<>(ops, key, ops.promote(value, this.key, key));
  }

  Data<K, V> squash(Data<K, V> that) {
    assert Objects.equals(this.key, that.key);
    return new Data<>(ops, key, ops.squash(this.value, that.value));
  }

  @Override
  public <W> Data<K, W> map(Function<? super V, ? extends W> f, Ops<K, W> ops) {
    return new Data<>(ops, key, f.apply(value));
  }

}