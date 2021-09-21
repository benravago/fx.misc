package fx.react.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fx.react.util.Lists;
import javafx.collections.ListChangeListener;

public final class ListChangeAccumulator<E> implements ListModificationSequence<E> {

  QuasiListChange<E> modifications = new QuasiListChange<>();

  public ListChangeAccumulator() {}

  public ListChangeAccumulator(QuasiChange<E> change) {
    modifications = new QuasiListChange<>(change);
  }

  @Override
  public ListChangeAccumulator<E> asListChangeAccumulator() {
    return this;
  }

  @Override
  public QuasiChange<E> asListChange() {
    return fetch();
  }

  @Override
  public List<QuasiModification<? extends E>> getModifications() {
    return Collections.unmodifiableList(modifications);
  }

  public boolean isEmpty() {
    return modifications.isEmpty();
  }

  public QuasiChange<E> fetch() {
    var res = modifications;
    modifications = new QuasiListChange<>();
    return res;
  }

  public ListChangeAccumulator<E> drop(int n) {
    modifications.subList(0, n).clear();
    return this;
  }

  public ListChangeAccumulator<E> add(QuasiModification<? extends E> mod) {
    if (modifications.isEmpty()) {
      modifications.add(mod);
    } else {
      // find first and last overlapping modification
      var from = mod.getFrom();
      var to = from + mod.getRemovedSize();

      var firstOverlapping = 0;
      for (; firstOverlapping < modifications.size(); ++firstOverlapping) {
        if (modifications.get(firstOverlapping).getTo() >= from) {
          break;
        }
      }

      var lastOverlapping = modifications.size() - 1;
      for (; lastOverlapping >= 0; --lastOverlapping) {
        if (modifications.get(lastOverlapping).getFrom() <= to) {
          break;
        }
      }

      // offset modifications farther in the list
      var diff = mod.getTo() - mod.getFrom() - mod.getRemovedSize();
      offsetPendingModifications(lastOverlapping + 1, diff);

      // combine overlapping modifications into one
      if (lastOverlapping < firstOverlapping) { // no overlap
        modifications.add(firstOverlapping, mod);
      } else { // overlaps one or more former modifications
        var overlapping = modifications.subList(firstOverlapping, lastOverlapping + 1);
        var joined = join(overlapping, mod.getRemoved(), mod.getFrom());
        var newMod = combine(joined, mod);
        overlapping.clear();
        modifications.add(firstOverlapping, newMod);
      }
    }

    return this;
  }

  public ListChangeAccumulator<E> add(QuasiChange<? extends E> change) {
    for (var mod : change) {
      add(mod);
    }
    return this;
  }

  public ListChangeAccumulator<E> add(ListChangeListener.Change<? extends E> change) {
    while (change.next()) {
      add(QuasiModification.fromCurrentStateOf(change));
    }
    return this;
  }

  void offsetPendingModifications(int from, int offset) {
    modifications.subList(from, modifications.size())
      .replaceAll(mod -> new QuasiListModification<>(mod.getFrom() + offset, mod.getRemoved(), mod.getAddedSize()));
  }

  static <E> QuasiModification<? extends E> join(List<QuasiModification<? extends E>> mods, List<? extends E> gone, int goneOffset) {
    if (mods.size() == 1) {
      return mods.get(0);
    }
    var removedLists = new ArrayList<List<? extends E>>(2 * mods.size() - 1);
    var prev = mods.get(0);
    var from = prev.getFrom();
    removedLists.add(prev.getRemoved());
    for (var i = 1; i < mods.size(); ++i) {
      var m = mods.get(i);
      removedLists.add(gone.subList(prev.getTo() - goneOffset, m.getFrom() - goneOffset));
      removedLists.add(m.getRemoved());
      prev = m;
    }
    var removed = Lists.concat(removedLists);
    return new QuasiListModification<>(from, removed, prev.getTo() - from);
  }

  static <E> QuasiModification<E> combine(QuasiModification<? extends E> former, QuasiModification<? extends E> latter) {
    if (latter.getFrom() >= former.getFrom() && latter.getFrom() + latter.getRemovedSize() <= former.getTo()) {
      // latter is within former
      var removed = former.getRemoved();
      var addedSize = former.getAddedSize() - latter.getRemovedSize() + latter.getAddedSize();
      return new QuasiListModification<>(former.getFrom(), removed, addedSize);
    }
    else if (latter.getFrom() <= former.getFrom() && latter.getFrom() + latter.getRemovedSize() >= former.getTo()) {
      // former is within latter
      var removed = Lists.concat(
        latter.getRemoved().subList(0, former.getFrom() - latter.getFrom()),
        former.getRemoved(),
        latter.getRemoved().subList(former.getTo() - latter.getFrom(),
        latter.getRemovedSize())
      );
      return new QuasiListModification<>(latter.getFrom(), removed, latter.getAddedSize());
    }
    else if (latter.getFrom() >= former.getFrom()) {
      // latter overlaps to the right
      var removed = Lists.concat(
        former.getRemoved(),
        latter.getRemoved().subList(former.getTo() - latter.getFrom(),
        latter.getRemovedSize())
      );
      return new QuasiListModification<>(former.getFrom(), removed, latter.getTo() - former.getFrom());
    }
    else {
      // latter overlaps to the left
      var removed = Lists.concat(
        latter.getRemoved().subList(0, former.getFrom() - latter.getFrom()),
        former.getRemoved()
      );
      var addedSize = former.getTo() - latter.getRemovedSize() + latter.getAddedSize() - latter.getFrom();
      return new QuasiListModification<>(latter.getFrom(), removed, addedSize);
    }
  }

}