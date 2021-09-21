package fx.input.tree;

import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

class Empty<K, V> extends PrefixTree<K, V> {

  Empty(Ops<K, V> ops) {
    super(ops);
  }

  @Override
  public Stream<Entry<K, V>> entries() {
    return Stream.empty();
  }

  @Override
  public PrefixTree<K, V> insert(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine) {
    return insertInside(key, value, combine);
  }

  @Override
  PrefixTree<K, V> insertInside(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine) {
    return new Data<>(ops, key, value);
  }

  @Override
  public <W> PrefixTree<K, W> map(Function<? super V, ? extends W> f, Ops<K, W> ops) {
    return new Empty<>(ops);
  }

}