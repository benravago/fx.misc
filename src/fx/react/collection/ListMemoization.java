package fx.react.collection;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;

import fx.react.Subscription;
import fx.react.util.Lists;
import fx.util.sparse.SparseList;

class ListMemoization<E> extends LiveListBase<E> implements MemoizedList<E>, UnmodifiableByDefaultLiveList<E> {

  class MemoizedView extends LiveListBase<E> implements UnmodifiableByDefaultLiveList<E> {
    @Override protected Subscription observeInputs() { return ListMemoization.this.pin(); }
    @Override public E get(int index) { return sparseList.getPresent(index); }
    @Override public int size() { return sparseList.getPresentCount(); }
    void prepareNotifications(QuasiChange<? extends E> change) { enqueueNotifications(change); }
    void prepareNotifications(QuasiModification<? extends E> mod) { enqueueNotifications(mod.asListChange()); }
    void publishNotifications() { notifyObservers(); }
  }

  final SparseList<E> sparseList = new SparseList<>();
  final MemoizedView memoizedItems = new MemoizedView();
  final ObservableList<E> source;

  ListMemoization(ObservableList<E> source) {
    this.source = source;
  }

  @Override
  protected Subscription observeInputs() {
    sparseList.insertVoid(0, source.size());
    return LiveList.<E>observeQuasiChanges(source, this::sourceChanged).and(sparseList::clear);
  }

  void sourceChanged(QuasiChange<? extends E> qc) {
    var acc = new ListChangeAccumulator<E>();
    for (var mod : qc) {
      var from = mod.getFrom();
      var removedSize = mod.getRemovedSize();
      var memoFrom = sparseList.getPresentCountBefore(from);
      var memoRemoved = sparseList.collect(from, from + removedSize);
      sparseList.spliceByVoid(from, from + removedSize, mod.getAddedSize());
      acc.add(new QuasiListModification<>(memoFrom, memoRemoved, 0));
    }
    memoizedItems.prepareNotifications(acc.fetch());
    notifyObservers(qc);
    memoizedItems.publishNotifications();
  }

  @Override
  public E get(int index) {
    if (!isObservingInputs()) { // memoization is off
      return source.get(index);
    } else if (sparseList.isPresent(index)) {
      return sparseList.getOrThrow(index);
    } else {
      var elem = source.get(index); // may cause recursive get(), so we need to check again for absence
      if (sparseList.setIfAbsent(index, elem)) {
        memoizedItems.fireElemInsertion(sparseList.getPresentCountBefore(index));
      }
      return elem;
    }
  }

  @Override
  public void force(int from, int to) {
    if (!isObservingInputs()) { // memoization is off
      throw new IllegalStateException("Cannot force items when memoization is off."
        + " To turn memoization on, you have to be observing this" + " list or its memoizedItems.");
    }
    Lists.checkRange(from, to, size());
    for (var i = from; i < to; ++i) {
      if (!sparseList.isPresent(i)) {
        var elem = source.get(i);
        if (sparseList.setIfAbsent(i, elem)) {
          var presentBefore = sparseList.getPresentCountBefore(i);
          memoizedItems.prepareNotifications(ProperLiveList.elemInsertion(presentBefore));
        }
      }
    }
    memoizedItems.publishNotifications();
  }

  @Override
  public int size() {
    return source.size();
  }

  @Override
  public LiveList<E> memoizedItems() {
    return memoizedItems;
  }

  @Override
  public boolean isMemoized(int index) {
    return isObservingInputs() && sparseList.isPresent(index);
  }

  @Override
  public Optional<E> getIfMemoized(int index) {
    Lists.checkIndex(index, size());
    return isObservingInputs() ? sparseList.get(index) : Optional.empty();
  }

  @Override
  public int getMemoizedCountBefore(int position) {
    Lists.checkPosition(position, size());
    return isObservingInputs() ? sparseList.getPresentCountBefore(position) : 0;
  }

  @Override
  public int getMemoizedCountAfter(int position) {
    Lists.checkPosition(position, size());
    return isObservingInputs() ? sparseList.getPresentCountAfter(position) : 0;
  }

  @Override
  public int getMemoizedCount() {
    return memoizedItems.size();
  }

  @Override
  public void forget(int from, int to) {
    if (!isObservingInputs()) { // memoization is off
      throw new IllegalStateException("There is nothing to forget, because memoization is off."
        + " To turn memoization on, you have to be observing this" + " list or its memoizedItems.");
    }
    Lists.checkRange(from, to, size());
    var memoChangeFrom = sparseList.getPresentCountBefore(from);
    var memoRemoved = sparseList.collect(from, to);
    sparseList.spliceByVoid(from, to, to - from);
    memoizedItems.fireRemoveRange(memoChangeFrom, memoRemoved);
  }

  @Override
  public int indexOfMemoizedItem(int index) {
    return sparseList.indexOfPresentItem(index);
  }

  @Override
  public IndexRange getMemoizedItemsRange() {
    return sparseList.getPresentItemsRange();
  }

}
