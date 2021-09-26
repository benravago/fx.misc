package fx.rich.text;

import java.text.BreakIterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;
import javafx.util.Duration;

import fx.react.Subscription;
import fx.react.Suspendable;
import fx.react.SuspendableNo;
import fx.react.value.SuspendableVal;
import fx.react.value.Val;
import fx.react.value.Var;

import fx.rich.text.model.StyledDocument;

class CaretSelectionBinding<PS, SEG, S> implements CaretSelectionBind<PS, SEG, S> {

  // caret
  @Override
  public Var<Visibility> showCaretProperty() {
    return delegateCaret.showCaretProperty();
  }

  @Override
  public Visibility getShowCaret() {
    return delegateCaret.getShowCaret();
  }

  @Override
  public void setShowCaret(Visibility value) {
    delegateCaret.setShowCaret(value);
  }

  /* ********************************************************************** *
   *                                                                        *
   * Observables                                                            *
   *                                                                        *
   * Observables are "dynamic" (i.e. changing) characteristics of this      *
   * control. They are not directly settable by the client code, but change *
   * in response to user input and/or API actions.                          *
   *                                                                        *
   * ********************************************************************** */

  // caret
  @Override
  public ObservableValue<Integer> positionProperty() {
    return delegateCaret.positionProperty();
  }

  @Override
  public int getPosition() {
    return delegateCaret.getPosition();
  }

  @Override
  public ObservableValue<Integer> paragraphIndexProperty() {
    return delegateCaret.paragraphIndexProperty();
  }

  @Override
  public int getParagraphIndex() {
    return delegateCaret.getParagraphIndex();
  }

  @Override
  public ObservableValue<OptionalInt> lineIndexProperty() {
    return delegateCaret.lineIndexProperty();
  }

  @Override
  public OptionalInt getLineIndex() {
    return delegateCaret.getLineIndex();
  }

  @Override
  public ObservableValue<Integer> columnPositionProperty() {
    return delegateCaret.columnPositionProperty();
  }

  @Override
  public int getColumnPosition() {
    return delegateCaret.getColumnPosition();
  }

  @Override
  public ObservableValue<Boolean> visibleProperty() {
    return delegateCaret.visibleProperty();
  }

  @Override
  public boolean isVisible() {
    return delegateCaret.isVisible();
  }

  @Override
  public ObservableValue<Duration> blinkRateProperty() {
    return delegateCaret.blinkRateProperty();
  }

  @Override
  public Duration getBlinkRate() {
    return delegateCaret.getBlinkRate();
  }

  @Override
  public void setBlinkRate(Duration blinkRate) {
    delegateCaret.setBlinkRate(blinkRate);
  }

  @Override
  public ObservableValue<Optional<Bounds>> caretBoundsProperty() {
    return delegateCaret.caretBoundsProperty();
  }

  @Override
  public Optional<Bounds> getCaretBounds() {
    return delegateCaret.getCaretBounds();
  }

  @Override
  public void clearTargetOffset() {
    delegateCaret.clearTargetOffset();
  }

  @Override
  public ParagraphBox.CaretOffsetX getTargetOffset() {
    return delegateCaret.getTargetOffset();
  }

  // selection
  @Override
  public ObservableValue<IndexRange> rangeProperty() {
    return delegateSelection.rangeProperty();
  }

  @Override
  public IndexRange getRange() {
    return delegateSelection.getRange();
  }

  @Override
  public ObservableValue<Integer> lengthProperty() {
    return delegateSelection.lengthProperty();
  }

  @Override
  public int getLength() {
    return delegateSelection.getLength();
  }

  @Override
  public ObservableValue<Integer> paragraphSpanProperty() {
    return delegateSelection.paragraphSpanProperty();
  }

  @Override
  public int getParagraphSpan() {
    return delegateSelection.getParagraphSpan();
  }

  @Override
  public final ObservableValue<StyledDocument<PS, SEG, S>> selectedDocumentProperty() {
    return delegateSelection.selectedDocumentProperty();
  }

  @Override
  public final StyledDocument<PS, SEG, S> getSelectedDocument() {
    return delegateSelection.getSelectedDocument();
  }

  @Override
  public ObservableValue<String> selectedTextProperty() {
    return delegateSelection.selectedTextProperty();
  }

  @Override
  public String getSelectedText() {
    return delegateSelection.getSelectedText();
  }

  @Override
  public ObservableValue<Integer> startPositionProperty() {
    return delegateSelection.startPositionProperty();
  }

  @Override
  public int getStartPosition() {
    return delegateSelection.getStartPosition();
  }

  @Override
  public ObservableValue<Integer> startParagraphIndexProperty() {
    return delegateSelection.startParagraphIndexProperty();
  }

  @Override
  public int getStartParagraphIndex() {
    return delegateSelection.getStartParagraphIndex();
  }

  @Override
  public ObservableValue<Integer> startColumnPositionProperty() {
    return delegateSelection.startColumnPositionProperty();
  }

  @Override
  public int getStartColumnPosition() {
    return delegateSelection.getStartColumnPosition();
  }

  @Override
  public ObservableValue<Integer> endPositionProperty() {
    return delegateSelection.endPositionProperty();
  }

  @Override
  public int getEndPosition() {
    return delegateSelection.getEndPosition();
  }

  @Override
  public ObservableValue<Integer> endParagraphIndexProperty() {
    return delegateSelection.endParagraphIndexProperty();
  }

  @Override
  public int getEndParagraphIndex() {
    return delegateSelection.getEndParagraphIndex();
  }

  @Override
  public ObservableValue<Integer> endColumnPositionProperty() {
    return delegateSelection.endColumnPositionProperty();
  }

  @Override
  public int getEndColumnPosition() {
    return delegateSelection.getEndColumnPosition();
  }

  @Override
  public ObservableValue<Optional<Bounds>> selectionBoundsProperty() {
    return delegateSelection.selectionBoundsProperty();
  }

  @Override
  public Optional<Bounds> getSelectionBounds() {
    return delegateSelection.getSelectionBounds();
  }

  final CaretNode delegateCaret;

  @Override
  public CaretNode getUnderlyingCaret() {
    return delegateCaret;
  }

  @Override
  public String getCaretName() {
    return delegateCaret.getCaretName();
  }

  final Selection<PS, SEG, S> delegateSelection;

  @Override
  public Selection<PS, SEG, S> getUnderlyingSelection() {
    return delegateSelection;
  }

  @Override
  public String getSelectionName() {
    return delegateSelection.getSelectionName();
  }

  @Override
  public void configureSelectionPath(SelectionPath path) {
    delegateSelection.configureSelectionPath(path);
  }

  @Override
  public GenericStyledArea<PS, SEG, S> getArea() {
    return delegateSelection.getArea();
  }

  // caret selection bind
  final Val<Integer> anchorPosition;

  @Override
  public int getAnchorPosition() {
    return anchorPosition.getValue();
  }

  @Override
  public ObservableValue<Integer> anchorPositionProperty() {
    return anchorPosition;
  }

  final Val<Integer> anchorParIndex;

  @Override
  public int getAnchorParIndex() {
    return anchorParIndex.getValue();
  }

  @Override
  public ObservableValue<Integer> anchorParIndexProperty() {
    return anchorParIndex;
  }

  final Val<Integer> anchorColPosition;

  @Override
  public int getAnchorColPosition() {
    return anchorColPosition.getValue();
  }

  @Override
  public ObservableValue<Integer> anchorColPositionProperty() {
    return anchorColPosition;
  }

  final SuspendableNo beingUpdated = new SuspendableNo();

  public final boolean isBeingUpdated() {
    return beingUpdated.get();
  }

  public final ObservableValue<Boolean> beingUpdatedProperty() {
    return beingUpdated;
  }

  final Var<BooleanEvent> internalStartedByAnchor = Var.newSimpleVar(new BooleanEvent(true));
  final SuspendableVal<BooleanEvent> startedByAnchor = internalStartedByAnchor.suspendable();

  boolean anchorIsStart() {
    return startedByAnchor.getValue().get();
  }

  class BooleanEvent { // See #874
    BooleanEvent(boolean b) { value = b; }
    boolean get() { return value; }
    final boolean value;
  }

  Subscription subscription = () -> {};

  CaretSelectionBinding(String caretName, String selectionName, GenericStyledArea<PS, SEG, S> area) {
    this(caretName, selectionName, area, new IndexRange(0, 0));
  }

  CaretSelectionBinding(String caretName, String selectionName, GenericStyledArea<PS, SEG, S> area, IndexRange startingRange) {
    this((updater) -> new CaretNode(caretName, area, updater, startingRange.getStart()),
         (updater) -> new SelectionBase<>(selectionName, area, startingRange, updater));
  }

  CaretSelectionBinding(Function<SuspendableNo, ? extends CaretNode> createCaret, Function<SuspendableNo, Selection<PS, SEG, S>> createSelection) {
    var delegateUpdater = new SuspendableNo();
    delegateCaret = createCaret.apply(delegateUpdater);
    delegateSelection = createSelection.apply(delegateUpdater);
    if (delegateCaret.getArea() != delegateSelection.getArea()) {
      throw new IllegalArgumentException(String.format(
        "Caret and Selection must be asociated with the same area. Caret area = %s | Selection area = %s",
        delegateCaret.getArea(), delegateSelection.getArea()));
    }

    var anchorPositions = startedByAnchor.flatMap(
      b -> b.get() ? Val.constant(new int[]{ getStartPosition(), getStartParagraphIndex(), getStartColumnPosition() })
                   : Val.constant(new int[]{ getEndPosition(), getEndParagraphIndex(), getEndColumnPosition() })
    );
    anchorPosition = anchorPositions.map(a -> a[0]);
    anchorParIndex = anchorPositions.map(a -> a[1]);
    anchorColPosition = anchorPositions.map(a -> a[2]);

    var omniSuspendable = Suspendable.combine(
      // first, so it's released last
      beingUpdated,
      startedByAnchor,
      // last, so it's released before startedByAnchor, so that anchor's values are correct
      delegateUpdater
    );
    subscription = omniSuspendable.suspendWhen(getArea().beingUpdatedProperty());
  }

  /* ********************************************************************** *
   *                                                                        *
   * Actions                                                                *
   *                                                                        *
   * Actions change the state of this control. They typically cause a       *
   * change of one or more observables and/or produce an event.             *
   *                                                                        *
   * ********************************************************************** */

  // caret
  @Override
  public void moveBreaksForwards(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingForwards(numOfBreaks, breakIterator, getPosition());
    moveTo(position, NavigationActions.SelectionPolicy.CLEAR);
  }

  @Override
  public void moveBreaksBackwards(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingBackwards(numOfBreaks, breakIterator, getPosition());
    moveTo(position, NavigationActions.SelectionPolicy.CLEAR);
  }

  // selection
  @Override
  public void selectRange(int startPosition, int endPosition) {
    doSelect(startPosition, endPosition, anchorIsStart());
  }

  @Override
  public void selectRange(int startParagraphIndex, int startColPosition, int endParagraphIndex, int endColPosition) {
    selectRange(textPosition(startParagraphIndex, startColPosition), textPosition(endParagraphIndex, endColPosition));
  }

  @Override
  public void updateStartBy(int amount, Direction direction) {
    var updatedStart = direction == Direction.LEFT ? getStartPosition() - amount : getStartPosition() + amount;
    selectRange(updatedStart, getEndPosition());
  }

  @Override
  public void updateEndBy(int amount, Direction direction) {
    var updatedEnd = direction == Direction.LEFT ? getEndPosition() - amount : getEndPosition() + amount;
    selectRange(getStartPosition(), updatedEnd);
  }

  @Override
  public void updateStartTo(int position) {
    selectRange(position, getEndPosition());
  }

  @Override
  public void updateStartTo(int paragraphIndex, int columnPosition) {
    updateStartTo(textPosition(paragraphIndex, columnPosition));
  }

  @Override
  public void updateStartByBreaksForward(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingForwards(numOfBreaks, breakIterator, getStartPosition());
    updateStartTo(position);
  }

  @Override
  public void updateStartByBreaksBackward(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingBackwards(numOfBreaks, breakIterator, getStartPosition());
    updateStartTo(position);
  }

  @Override
  public void updateEndTo(int position) {
    selectRange(getStartPosition(), position);
  }

  @Override
  public void updateEndTo(int paragraphIndex, int columnPosition) {
    updateEndTo(textPosition(paragraphIndex, columnPosition));
  }

  @Override
  public void updateEndByBreaksForward(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingForwards(numOfBreaks, breakIterator, getStartPosition());
    updateEndTo(position);
  }

  @Override
  public void updateEndByBreaksBackward(int numOfBreaks, BreakIterator breakIterator) {
    if (getAreaLength() == 0) {
      return;
    }
    breakIterator.setText(getArea().getText());
    var position = calculatePositionViaBreakingBackwards(numOfBreaks, breakIterator, getStartPosition());
    updateEndTo(position);
  }

  @Override
  public void selectAll() {
    selectRange(0, getAreaLength());
  }

  @Override
  public void selectParagraph(int paragraphIndex) {
    var start = textPosition(paragraphIndex, 0);
    var end = getArea().getParagraphLength(paragraphIndex);
    selectRange(start, end);
  }

  @Override
  public void selectWord(int wordPositionInArea) {
    if (getAreaLength() == 0) {
      return;
    }
    var breakIterator = BreakIterator.getWordInstance(getArea().getLocale());
    breakIterator.setText(getArea().getText());
    var start = calculatePositionViaBreakingBackwards(1, breakIterator, wordPositionInArea);
    var end = calculatePositionViaBreakingForwards(1, breakIterator, wordPositionInArea);
    selectRange(start, end);
  }

  @Override
  public void deselect() {
    selectRangeExpl(getPosition(), getPosition());
  }

  // caret selection bind
  @Override
  public void selectRangeExpl(int anchorParagraph, int anchorColumn, int caretParagraph, int caretColumn) {
    selectRangeExpl(textPosition(anchorParagraph, anchorColumn), textPosition(caretParagraph, caretColumn));
  }

  @Override
  public void selectRangeExpl(int anchorPosition, int caretPosition) {
    if (anchorPosition <= caretPosition) {
      doSelect(anchorPosition, caretPosition, true);
    } else {
      doSelect(caretPosition, anchorPosition, false);
    }
  }

  @Override
  public void moveTo(int pos, NavigationActions.SelectionPolicy selectionPolicy) {
    switch (selectionPolicy) {
      case CLEAR -> {
        selectRangeExpl(pos, pos);
      }
      case ADJUST -> {
        selectRangeExpl(getAnchorPosition(), pos);
      }
      case EXTEND -> {
        var sel = getRange();
        var anchor = (pos <= sel.getStart()) ? sel.getEnd()
                   : (pos >= sel.getEnd()) ? sel.getStart()
                   : getAnchorPosition();
        selectRangeExpl(anchor, pos);
      }
    }
  }

  @Override
  public void moveTo(int paragraphIndex, int columnIndex, NavigationActions.SelectionPolicy selectionPolicy) {
    moveTo(textPosition(paragraphIndex, columnIndex), selectionPolicy);
  }

  @Override
  public void moveToPrevChar(NavigationActions.SelectionPolicy selectionPolicy) {
    if (getPosition() > 0) {
      var newCaretPos = Character.offsetByCodePoints(getArea().getText(), getPosition(), -1);
      moveTo(newCaretPos, selectionPolicy);
    }
  }

  @Override
  public void moveToNextChar(NavigationActions.SelectionPolicy selectionPolicy) {
    if (getPosition() < getAreaLength()) {
      var newCaretPos = Character.offsetByCodePoints(getArea().getText(), getPosition(), 1);
      moveTo(newCaretPos, selectionPolicy);
    }
  }

  @Override
  public void moveToParStart(NavigationActions.SelectionPolicy selectionPolicy) {
    moveTo(getPosition() - getColumnPosition(), selectionPolicy);
  }

  @Override
  public void moveToParEnd(NavigationActions.SelectionPolicy selectionPolicy) {
    moveTo(getPosition() - getColumnPosition() + getArea().getParagraphLength(getParagraphIndex()), selectionPolicy);
  }

  @Override
  public void moveToAreaStart(NavigationActions.SelectionPolicy selectionPolicy) {
    moveTo(0, selectionPolicy);
  }

  @Override
  public void moveToAreaEnd(NavigationActions.SelectionPolicy selectionPolicy) {
    moveTo(getArea().getLength(), selectionPolicy);
  }

  @Override
  public void displaceCaret(int position) {
    doUpdate(() -> delegateCaret.moveTo(position));
  }

  @Override
  public void displaceSelection(int startPosition, int endPosition) {
    doUpdate(() -> {
      delegateSelection.selectRange(startPosition, endPosition);
      internalStartedByAnchor.setValue(new BooleanEvent(startPosition < endPosition));
    });
  }

  @Override
  public void dispose() {
    subscription.unsubscribe();
  }

  /* ********************************************************************** *
   *                                                                        *
   * methods                                                        *
   *                                                                        *
   * ********************************************************************** */

  void doSelect(int startPosition, int endPosition, boolean anchorIsStart) {
    doUpdate(() -> {
      delegateSelection.selectRange(startPosition, endPosition);
      internalStartedByAnchor.setValue(new BooleanEvent(anchorIsStart));
      delegateCaret.moveTo(anchorIsStart ? endPosition : startPosition);
    });
  }

  void doUpdate(Runnable updater) {
    if (isBeingUpdated()) {
      updater.run();
    } else {
      getArea().beingUpdatedProperty().suspendWhile(updater);
    }
  }

  int textPosition(int paragraphIndex, int columnPosition) {
    return getArea().position(paragraphIndex, columnPosition).toOffset();
  }

  int getAreaLength() {
    return getArea().getLength();
  }

  /** Assumes that {@code getArea().getLength != 0} is true and {@link BreakIterator#setText(String)} has been called */
  int calculatePositionViaBreakingForwards(int numOfBreaks, BreakIterator breakIterator, int position) {
    breakIterator.following(position);
    for (var i = 1; i < numOfBreaks; i++) {
      breakIterator.next(numOfBreaks);
    }
    return breakIterator.current();
  }

  /** Assumes that {@code getArea().getLength != 0} is true and {@link BreakIterator#setText(String)} has been called */
  int calculatePositionViaBreakingBackwards(int numOfBreaks, BreakIterator breakIterator, int position) {
    breakIterator.preceding(position);
    for (var i = 1; i < numOfBreaks; i++) {
      breakIterator.previous();
    }
    return breakIterator.current();
  }

}
