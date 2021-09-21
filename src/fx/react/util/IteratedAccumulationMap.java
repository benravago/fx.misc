package fx.react.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javafx.util.Pair;

class IteratedAccumulationMap<K, V, A> implements AccumulationMap<K, V, A> {

  K currentKey = null;
  A currentAccumulatedValue = null;

  Iterator<K> it;
  V value;

  IteratedAccumulationMap(Iterator<K> keys, V value) {
    this.it = keys;
    this.value = value;
  }

  @Override
  public boolean isEmpty() {
    return currentKey == null && !it.hasNext();
  }

  @Override
  public Pair<K, A> peek(AccumulationFacility<V, A> af) {
    if (currentKey == null) {
      currentKey = it.next();
      currentAccumulatedValue = af.initialAccumulator(value);
    }
    return new Pair<>(currentKey, currentAccumulatedValue);
  }

  @Override
  public AccumulationMap<K, V, A> dropPeeked() {
    checkPeeked();
    currentKey = null;
    currentAccumulatedValue = null;
    return this;
  }

  @Override
  public AccumulationMap<K, V, A> updatePeeked(A newAccumulatedValue) {
    checkPeeked();
    currentAccumulatedValue = newAccumulatedValue;
    return this;
  }

  @Override
  public AccumulationMap<K, V, A> addAll(Iterator<K> keys, V value, AccumulationFacility<V, A> af) {
    if (isEmpty()) {
      this.it = keys;
      this.value = value;
      return this;
    } else if (!keys.hasNext()) {
      return this;
    } else {
      var res = new HashedAccumulationMap<K, V, A>();
      if (currentKey != null) {
        res.put(currentKey, currentAccumulatedValue);
      }
      return res.addAll(it, this.value, af).addAll(keys, value, af);
    }
  }

  void checkPeeked() {
    if (currentKey == null) {
      throw new NoSuchElementException("No peeked value present. Use peek() first.");
    }
  }

}
