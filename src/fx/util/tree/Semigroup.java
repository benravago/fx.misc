package fx.util.tree;

public interface Semigroup<T> {
  T reduce(T left, T right);
}