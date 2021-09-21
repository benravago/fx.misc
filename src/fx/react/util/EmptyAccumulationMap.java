package fx.react.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javafx.util.Pair;

class EmptyAccumulationMap<K, V, A> implements AccumulationMap<K, V, A> {

  private EmptyAccumulationMap() {}
  private static class Singleton {
    static final EmptyAccumulationMap<?,?,?> INSTANCE = new EmptyAccumulationMap<>();
  }

  @SuppressWarnings("unchecked")
  static <K, V, A> AccumulationMap<K, V, A> instance() {
    return (AccumulationMap<K,V,A>) Singleton.INSTANCE;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public Pair<K, A> peek(AccumulationFacility<V, A> af) {
    throw new NoSuchElementException();
  }

  @Override
  public AccumulationMap<K, V, A> dropPeeked() {
    throw new NoSuchElementException();
  }

  @Override
  public AccumulationMap<K, V, A> updatePeeked(A newAccumulatedValue) {
    throw new NoSuchElementException();
  }

  @Override
  public AccumulationMap<K, V, A> addAll(Iterator<K> keys, V value, AccumulationFacility<V, A> af) {
    return new IteratedAccumulationMap<>(keys, value);
  }

}