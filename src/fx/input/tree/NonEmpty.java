package fx.input.tree;

import java.util.function.BiFunction;
import java.util.function.Function;

abstract class NonEmpty<K, V> extends PrefixTree<K, V> {

  NonEmpty(Ops<K, V> ops) {
    super(ops);
  }

  abstract K getPrefix();

  abstract Data<K, V> collapse();

  @Override
  public abstract <W> NonEmpty<K, W> map(Function<? super V, ? extends W> f, Ops<K, W> ops);

  @Override
  abstract NonEmpty<K, V> insertInside(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine);

  @Override
  public PrefixTree<K, V> insert(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine) {
    if (ops.isPrefixOf(key, getPrefix())) { // key is a prefix of this tree
      return new Data<>(ops, key, value).insertInside(collapse(), flip(combine));
    } else if (ops.isPrefixOf(getPrefix(), key)) { // key is inside this tree
      return insertInside(key, value, combine);
    } else {
      return new Branch<>(ops, this, new Data<>(ops, key, value));
    }
  }

}