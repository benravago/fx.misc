package fx.input.tree;

public interface Ops<K, V> {
  boolean isPrefixOf(K k1, K k2);
  K commonPrefix(K k1, K k2);
  V promote(V v, K oldKey, K newKey);
  V squash(V v1, V v2);
}