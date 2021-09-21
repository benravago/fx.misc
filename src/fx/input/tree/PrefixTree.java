package fx.input.tree;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Prefix tree (Trie) with an additional property that no data is stored in
 * internal nodes.
 *
 * @param <K> type of "strings" used to index values
 * @param <V> type of values (data) indexed by this trie
 */
public abstract class PrefixTree<K, V> {

  public static <K, V> PrefixTree<K, V> empty(Ops<K, V> ops) {
    return new Empty<>(ops);
  }

  static <A, B, C> BiFunction<B, A, C> flip(BiFunction<A, B, C> f) {
    return (a, b) -> f.apply(b, a);
  }

  final Ops<K, V> ops;

  protected PrefixTree(Ops<K, V> ops) {
    this.ops = ops;
  }

  public abstract Stream<Map.Entry<K, V>> entries();

  public abstract PrefixTree<K, V> insert(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine);

  public abstract <W> PrefixTree<K, W> map(Function<? super V, ? extends W> f, Ops<K, W> ops);

  public final PrefixTree<K, V> map(Function<? super V, ? extends V> f) {
    return map(f, ops);
  }

  abstract PrefixTree<K, V> insertInside(K key, V value, BiFunction<? super V, ? super V, ? extends V> combine);
}
