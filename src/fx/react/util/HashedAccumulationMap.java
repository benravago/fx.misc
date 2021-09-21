package fx.react.util;

import java.util.HashMap;
import java.util.Iterator;

import javafx.util.Pair;

class HashedAccumulationMap<K, V, A> extends HashMap<K, A> implements AccumulationMap<K, V, A> {

  @Override
  public Pair<K, A> peek(AccumulationFacility<V, A> af) {
    var key = pickKey();
    var acc = this.get(key);
    return new Pair<>(key, acc);
  }

  @Override
  public AccumulationMap<K, V, A> dropPeeked() {
    var key = pickKey();
    this.remove(key);
    return this;
  }

  @Override
  public AccumulationMap<K, V, A> updatePeeked(A newAccumulatedValue) {
    var key = pickKey();
    this.put(key, newAccumulatedValue);
    return this;
  }

  @Override
  public AccumulationMap<K, V, A> addAll(Iterator<K> keys, V value, AccumulationFacility<V, A> af) {
    while (keys.hasNext()) {
      var key = keys.next();
      if (this.containsKey(key)) {
        var accum = this.get(key);
        accum = af.reduce(accum, value);
        this.put(key, accum);
      } else {
        var accum = af.initialAccumulator(value);
        this.put(key, accum);
      }
    }
    return this;
  }

  private K pickKey() {
    return this.keySet().iterator().next();
  }

  private static final long serialVersionUID = 1L;
}