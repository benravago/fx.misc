package fx.undo.manager;

import java.util.NoSuchElementException;

public class ZeroSizeChangeQueue<C> implements ChangeQueue<C> {

  class Position implements QueuePosition {

    final long rev;

    Position(long seq) {
      this.rev = seq;
    }

    @Override
    public boolean isValid() {
      return rev == ZeroSizeChangeQueue.this.revision;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof ZeroSizeChangeQueue<?>.Position otherPos)
        ? getQueue() == otherPos.getQueue() && rev == otherPos.rev : false;
    }

    ZeroSizeChangeQueue<C> getQueue() {
      return ZeroSizeChangeQueue.this;
    }
  }

  long revision = 0;

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public boolean hasPrev() {
    return false;
  }

  @Override
  public C peekNext() {
    throw new NoSuchElementException();
  }

  @Override
  public C next() {
    throw new NoSuchElementException();
  }

  @Override
  public C peekPrev() {
    throw new NoSuchElementException();
  }

  @Override
  public C prev() {
    throw new NoSuchElementException();
  }

  @Override
  @SafeVarargs
  public final void push(C... changes) {
    ++revision;
  }

  @Override
  public QueuePosition getCurrentPosition() {
    return new Position(revision);
  }

  @Override
  public void forgetHistory() {
    // there is nothing to forget
  }

}
