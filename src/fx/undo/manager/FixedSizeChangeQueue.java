package fx.undo.manager;

import java.util.NoSuchElementException;

public class FixedSizeChangeQueue<C> implements ChangeQueue<C> {

  class Position implements QueuePosition {

    final int arrayPos;
    final long rev;

    Position(int arrayPos, long rev) {
      this.arrayPos = arrayPos;
      this.rev = rev;
    }

    @Override
    public boolean isValid() {
      var pos = relativize(arrayPos);
      if (pos <= size) {
        return rev == fetchRevisionForPosition(pos)
                // if the queue is full, then position 0 can also mean the last position ( = capacity)
                || pos == 0 && size == capacity && rev == fetchRevisionForPosition(capacity);
      } else {
        return false;
      }
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof FixedSizeChangeQueue<?>.Position otherPos)
        ? getQueue() == otherPos.getQueue() && rev == otherPos.rev : false;
    }

    FixedSizeChangeQueue<C> getQueue() {
      return FixedSizeChangeQueue.this;
    }
  }

  final RevisionedChange<C>[] changes;
  final int capacity;
  int start = 0;
  int size = 0;

  // current position is always from the interval [0, size],
  // i.e. not offset by start
  int currentPosition = 0;

  long revision = 0;
  long zeroPositionRevision = revision;

  @SuppressWarnings("unchecked")
  public FixedSizeChangeQueue(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must be positive");
    }
    this.capacity = capacity;
    this.changes = new RevisionedChange[capacity];
  }

  @Override
  public boolean hasNext() {
    return currentPosition < size;
  }

  @Override
  public boolean hasPrev() {
    return currentPosition > 0;
  }

  @Override
  public C peekNext() {
    if (currentPosition < size) {
      return fetch(currentPosition).getChange();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public C next() {
    var c = peekNext();
    currentPosition += 1;
    return c;
  }

  @Override
  public C peekPrev() {
    if (currentPosition > 0) {
      return fetch(currentPosition - 1).getChange();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public C prev() {
    C c = peekPrev();
    currentPosition -= 1;
    return c;
  }

  @Override
  public void forgetHistory() {
    zeroPositionRevision = fetchRevisionForPosition(currentPosition);
    start = arrayIndex(currentPosition);
    size -= currentPosition;
    currentPosition = 0;
  }

  @Override
  @SafeVarargs
  public final void push(C... changes) {
    RevisionedChange<C> lastOverwrittenChange = null;
    for (var c : changes) {
      var revC = new RevisionedChange<C>(c, ++revision);
      lastOverwrittenChange = put(currentPosition++, revC);
    }
    if (currentPosition > capacity) {
      start = arrayIndex(currentPosition);
      currentPosition = capacity;
      size = capacity;
      zeroPositionRevision = lastOverwrittenChange.getRevision();
    } else {
      size = currentPosition;
    }
  }

  @Override
  public QueuePosition getCurrentPosition() {
    var rev = fetchRevisionForPosition(currentPosition);
    return new Position(arrayIndex(currentPosition), rev);
  }

  long fetchRevisionForPosition(int position) {
    return (position == 0) ? zeroPositionRevision : fetch(position - 1).getRevision();
  }

  RevisionedChange<C> fetch(int position) {
    return changes[arrayIndex(position)];
  }

  RevisionedChange<C> put(int position, RevisionedChange<C> c) {
    var old = changes[arrayIndex(position)];
    changes[arrayIndex(position)] = c;
    return old;
  }

  // returns a number from [0..capacity-1]
  int arrayIndex(int queuePosition) {
    return (start + queuePosition) % capacity;
  }

  // inverse of arrayPos
  int relativize(int arrayPos) {
    return (arrayPos - start + capacity) % capacity;
  }

}
