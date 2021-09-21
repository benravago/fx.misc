package fx.undo.manager;

import java.util.ArrayList;

public class UnlimitedChangeQueue<C> implements ChangeQueue<C> {

  class Position implements QueuePosition {
    final int allTimePos;
    final long rev;

    Position(int allTimePos, long rev) {
      this.allTimePos = allTimePos;
      this.rev = rev;
    }

    @Override
    public boolean isValid() {
      var pos = allTimePos - forgottenCount;
      return (0 <= pos && pos <= changes.size()) ? rev == revisionForPosition(pos) : false;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof UnlimitedChangeQueue<?>.Position otherPos)
        ? getQueue() == otherPos.getQueue() && rev == otherPos.rev : false;
    }

    UnlimitedChangeQueue<C> getQueue() {
      return UnlimitedChangeQueue.this;
    }
  }

  final ArrayList<RevisionedChange<C>> changes = new ArrayList<>();
  int currentPosition = 0;

  long revision = 0;
  long zeroPositionRevision = revision;
  int forgottenCount = 0;

  @Override
  public final boolean hasNext() {
    return currentPosition < changes.size();
  }

  @Override
  public final boolean hasPrev() {
    return currentPosition > 0;
  }

  @Override
  public final C peekNext() {
    return changes.get(currentPosition).getChange();
  }

  @Override
  public final C next() {
    return changes.get(currentPosition++).getChange();
  }

  @Override
  public final C peekPrev() {
    return changes.get(currentPosition - 1).getChange();
  }

  @Override
  public final C prev() {
    return changes.get(--currentPosition).getChange();
  }

  @Override
  public void forgetHistory() {
    if (currentPosition > 0) {
      zeroPositionRevision = revisionForPosition(currentPosition);
      var newSize = changes.size() - currentPosition;
      for (var i = 0; i < newSize; ++i) {
        changes.set(i, changes.get(currentPosition + i));
      }
      changes.subList(newSize, changes.size()).clear();
      forgottenCount += currentPosition;
      currentPosition = 0;
    }
  }

  @Override
  @SafeVarargs
  public final void push(C... changes) {
    this.changes.subList(currentPosition, this.changes.size()).clear();
    for (var c : changes) {
      RevisionedChange<C> revC = new RevisionedChange<>(c, ++revision);
      this.changes.add(revC);
    }
    currentPosition += changes.length;
  }

  @Override
  public QueuePosition getCurrentPosition() {
    return new Position(forgottenCount + currentPosition, revisionForPosition(currentPosition));
  }

  long revisionForPosition(int position) {
    return position == 0 ? zeroPositionRevision : changes.get(position - 1).getRevision();
  }

}
