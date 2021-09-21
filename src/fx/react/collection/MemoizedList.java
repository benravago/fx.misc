package fx.react.collection;

import java.util.Optional;

import javafx.scene.control.IndexRange;

public interface MemoizedList<E> extends LiveList<E> {

  LiveList<E> memoizedItems();
  boolean isMemoized(int index);
  Optional<E> getIfMemoized(int index);
  int getMemoizedCount();
  int getMemoizedCountBefore(int position);
  int getMemoizedCountAfter(int position);
  void forget(int from, int to);
  int indexOfMemoizedItem(int index);
  IndexRange getMemoizedItemsRange();
  void force(int from, int to);

}
