package fx.react.util;

import java.util.Iterator;

import javafx.util.Pair;

/**
 * Accumulation map.
 *
 * @param <K> key type
 * @param <V> type of individual (non-accumulated) values
 * @param <A> type of accumulated values
 */
public interface AccumulationMap<K, V, A> {

  static <K, V, A> AccumulationMap<K, V, A> empty() {
    return EmptyAccumulationMap.instance();
  }

  boolean isEmpty();

  Pair<K, A> peek(AccumulationFacility<V, A> af);

  AccumulationMap<K, V, A> dropPeeked();

  AccumulationMap<K, V, A> updatePeeked(A newAccumulatedValue);

  AccumulationMap<K, V, A> addAll(Iterator<K> keys, V value, AccumulationFacility<V, A> af);

}