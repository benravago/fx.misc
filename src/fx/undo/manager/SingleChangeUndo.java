package fx.undo.manager;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import fx.react.EventSource;
import fx.react.EventStream;
import fx.react.Subscription;
import fx.react.SuspendableNo;
import fx.react.value.Val;
import fx.react.value.ValBase;
import fx.undo.UndoManager;
import fx.undo.UndoPosition;
import fx.undo.manager.ChangeQueue.QueuePosition;

/**
 * Implementation for {@link UndoManager} for single changes. For multiple changes, see
 * {@link MultiChangeUndo}.
 *
 * @param <C> the type of change to undo/redo
 */
public class SingleChangeUndo<C> implements UndoManager<C> {

  class Position implements UndoPosition {

    final QueuePosition queuePos;

    Position(QueuePosition queuePos) {
      this.queuePos = queuePos;
    }

    @Override
    public void mark() {
      mark = queuePos;
      canMerge = false;
      atMarkedPosition.invalidate();
    }
    @Override
    public boolean isValid() {
      return queuePos.isValid();
    }
  }

  final ChangeQueue<C> queue;
  final Function<? super C, ? extends C> invert;
  final Consumer<C> apply;
  final BiFunction<C, C, Optional<C>> merge;
  final Predicate<C> isIdentity;
  final Subscription subscription;
  final SuspendableNo performingAction = new SuspendableNo();

  final EventSource<Void> invalidationRequests = new EventSource<Void>();

  final Val<C> nextUndo = new ValBase<C>() {
    @Override
    protected Subscription connect() {
      return invalidationRequests.subscribe(x -> invalidate());
    }
    @Override
    protected C computeValue() {
      return queue.hasPrev() ? queue.peekPrev() : null;
    }
  };

  final Val<C> nextRedo = new ValBase<C>() {
    @Override
    protected Subscription connect() {
      return invalidationRequests.subscribe(x -> invalidate());
    }
    @Override
    protected C computeValue() {
      return queue.hasNext() ? queue.peekNext() : null;
    }
  };

  final BooleanBinding atMarkedPosition = new BooleanBinding() {
    /*<init>*/ {
      invalidationRequests.addObserver(x -> this.invalidate());
    }
    @Override
    protected boolean computeValue() {
      return mark.equals(queue.getCurrentPosition());
    }
  };

  boolean canMerge;
  QueuePosition mark;
  C expectedChange = null;

  public SingleChangeUndo(ChangeQueue<C> queue, Function<? super C, ? extends C> invert, Consumer<C> apply, BiFunction<C, C, Optional<C>> merge, Predicate<C> isIdentity, EventStream<C> changeSource) {
    this(queue, invert, apply, merge, isIdentity, changeSource, Duration.ZERO);
  }

  public SingleChangeUndo(ChangeQueue<C> queue, Function<? super C, ? extends C> invert, Consumer<C> apply, BiFunction<C, C, Optional<C>> merge, Predicate<C> isIdentity, EventStream<C> changeSource, Duration preventMergeDelay) {
    this.queue = queue;
    this.invert = invert;
    this.apply = apply;
    this.merge = merge;
    this.isIdentity = isIdentity;
    this.mark = queue.getCurrentPosition();

    var mainSub = changeSource.subscribe(this::changeObserved);

    if (preventMergeDelay.isZero() || preventMergeDelay.isNegative()) {
      subscription = mainSub;
    } else {
      var sub2 = changeSource.successionEnds(preventMergeDelay).subscribe(ignore -> preventMerge());
      subscription = mainSub.and(sub2);
    }
  }

  @Override
  public void close() {
    subscription.unsubscribe();
  }

  @Override
  public boolean undo() {
    return applyChange(isUndoAvailable(), () -> invert.apply(queue.prev()));
  }

  @Override
  public boolean redo() {
    return applyChange(isRedoAvailable(), queue::next);
  }

  @Override
  public Val<C> nextUndoProperty() {
    return nextUndo;
  }

  @Override
  public Val<C> nextRedoProperty() {
    return nextRedo;
  }

  @Override
  public boolean isUndoAvailable() {
    return nextUndo.isPresent();
  }

  @Override
  public Val<Boolean> undoAvailableProperty() {
    return nextUndo.map(c -> true).orElseConst(false);
  }

  @Override
  public boolean isRedoAvailable() {
    return nextRedo.isPresent();
  }

  @Override
  public Val<Boolean> redoAvailableProperty() {
    return nextRedo.map(c -> true).orElseConst(false);
  }

  @Override
  public boolean isPerformingAction() {
    return performingAction.get();
  }

  @Override
  public ObservableBooleanValue performingActionProperty() {
    return performingAction;
  }

  @Override
  public boolean isAtMarkedPosition() {
    return atMarkedPosition.get();
  }

  @Override
  public ObservableBooleanValue atMarkedPositionProperty() {
    return atMarkedPosition;
  }

  @Override
  public UndoPosition getCurrentPosition() {
    return new Position(queue.getCurrentPosition());
  }

  @Override
  public void preventMerge() {
    canMerge = false;
  }

  @Override
  public void forgetHistory() {
    queue.forgetHistory();
    invalidateProperties();
  }

  /**
   * Helper method for reducing code duplication
   *
   * @param isChangeAvailable same as `isUndoAvailable()` [Undo] or `isRedoAvailable()` [Redo]
   * @param changeToApply same as `invert.apply(queue.prev())` [Undo] or `queue.next()` [Redo]
   * @throws IllegalStateException if the applied change was not reinserted into the event stream
   */
  boolean applyChange(boolean isChangeAvailable, Supplier<C> changeToApply) throws IllegalStateException {
    if (isChangeAvailable) {
      canMerge = false;
      // perform change
      var change = changeToApply.get();
      this.expectedChange = change;
      performingAction.suspendWhile(() -> apply.accept(change));
      if (this.expectedChange != null) {
        throw new IllegalStateException("Expected change not received:\n" + this.expectedChange
            + "\nThe most likely cause is that the apply action did not reinsert the change into the event stream.");
      }
      invalidateProperties();
      return true;
    } else {
      return false;
    }
  }

  void changeObserved(C change) {
    if (expectedChange == null) {
      if (!isIdentity.test(change)) {
        addChange(change);
      }
    } else if (expectedChange.equals(change)) {
      expectedChange = null;
    } else {
      throw new IllegalArgumentException(
          "Unexpected change received." + "\nExpected:\n" + expectedChange + "\nReceived:\n" + change);
    }
  }

  @SuppressWarnings("unchecked")
  void addChange(C change) {
    if (canMerge && queue.hasPrev()) {
      var prev = queue.prev();
      // attempt to merge the changes
      var merged = merge.apply(prev, change);
      if (merged.isPresent()) {
        if (isIdentity.test(merged.get())) {
          canMerge = false;
          queue.push(); // clears the future
        } else {
          canMerge = true;
          queue.push(merged.get());
        }
      } else {
        canMerge = true;
        queue.next();
        queue.push(change);
      }
    } else {
      queue.push(change);
      canMerge = true;
    }
    invalidateProperties();
  }

  void invalidateProperties() {
    invalidationRequests.push(null);
  }

}
