package fx.rich.text;

import java.util.function.Predicate;
import static java.lang.Character.*;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import fx.input.EventPattern;
import fx.input.InputHandler.Result;
import fx.input.template.InputMapTemplate;
import fx.react.value.Val;
import fx.react.value.Var;
import static fx.react.EventStreams.*;
import static fx.input.EventPattern.*;
import static fx.input.template.InputMapTemplate.*;

import fx.rich.text.NavigationActions.SelectionPolicy;
import static fx.rich.text.model.TwoDimensional.Bias.*;

/**
 * Controller for GenericStyledArea.
 */
class GenericStyledAreaBehavior {

  static final boolean isMac;
  static final boolean isWindows;
  /*<init>*/ static {
    var os = System.getProperty("os.name");
    isMac = os.startsWith("Mac");
    isWindows = os.startsWith("Windows");
  }

  static final InputMapTemplate<GenericStyledAreaBehavior, ? super Event> EVENT_TEMPLATE;
  static final Predicate<KeyEvent> controlKeysFilter;

  /*<init>*/ static {
    var selPolicy = isMac ? SelectionPolicy.EXTEND : SelectionPolicy.ADJUST;

    /*
     * KeyCodes are misinterpreted when using a different keyboard layout, for example:
     * on Dvorak: C results in KeyCode I, X -> B, and V -> .
     * and on German layouts: Z and Y are reportedly switched
     * so then editing commands such as Ctrl+C, or CMD+Z are incorrectly processed.
     * KeyCharacterCombination however does keyboard translation before matching.
     * This resolves issue #799
     */
    var SHORTCUT_A = new KeyCharacterCombination("a", SHORTCUT_DOWN);
    var SHORTCUT_C = new KeyCharacterCombination("c", SHORTCUT_DOWN);
    var SHORTCUT_V = new KeyCharacterCombination("v", SHORTCUT_DOWN);
    var SHORTCUT_X = new KeyCharacterCombination("x", SHORTCUT_DOWN);
    var SHORTCUT_Y = new KeyCharacterCombination("y", SHORTCUT_DOWN);
    var SHORTCUT_Z = new KeyCharacterCombination("z", SHORTCUT_DOWN);
    var SHORTCUT_SHIFT_Z = new KeyCharacterCombination("z", SHORTCUT_DOWN, SHIFT_DOWN);

    var editsBase = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent>sequence(
      // deletion
      consume(keyPressed(DELETE), GenericStyledAreaBehavior::deleteForward),
      consume(keyPressed(BACK_SPACE), GenericStyledAreaBehavior::deleteBackward),
      consume(keyPressed(BACK_SPACE, SHIFT_DOWN), GenericStyledAreaBehavior::deleteBackward),
      consume(keyPressed(DELETE, SHORTCUT_DOWN), GenericStyledAreaBehavior::deleteNextWord),
      consume(keyPressed(BACK_SPACE, SHORTCUT_DOWN), GenericStyledAreaBehavior::deletePrevWord),
      // cut
      consume(anyOf(keyPressed(CUT), keyPressed(SHORTCUT_X), keyPressed(DELETE, SHIFT_DOWN)), (b, e) -> b.view.cut()),
      // paste
      consume(anyOf(keyPressed(PASTE), keyPressed(SHORTCUT_V), keyPressed(INSERT, SHIFT_DOWN)), (b, e) -> b.view.paste()),
      // tab & newline
      consume(keyPressed(ENTER), (b, e) -> b.view.replaceSelection("\n")),
      consume(keyPressed(TAB), (b, e) -> b.view.replaceSelection("\t")),
      // undo/redo
      consume(keyPressed(SHORTCUT_Z), (b, e) -> b.view.undo()),
      consume(anyOf(keyPressed(SHORTCUT_Y), keyPressed(SHORTCUT_SHIFT_Z)), (b, e) -> b.view.redo()));

    var edits = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent>when(
      b -> b.view.isEditable(), editsBase
    );

    var verticalNavigation = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent>sequence(
      // vertical caret movement
      consume(anyOf(keyPressed(UP), keyPressed(KP_UP)), (b, e) -> b.prevLine(SelectionPolicy.CLEAR)),
      consume(anyOf(keyPressed(DOWN), keyPressed(KP_DOWN)), (b, e) -> b.nextLine(SelectionPolicy.CLEAR)),
      consume(keyPressed(PAGE_UP), (b, e) -> b.view.prevPage(SelectionPolicy.CLEAR)),
      consume(keyPressed(PAGE_DOWN), (b, e) -> b.view.nextPage(SelectionPolicy.CLEAR)),
      // vertical selection
      consume(anyOf(keyPressed(UP, SHIFT_DOWN), keyPressed(KP_UP, SHIFT_DOWN)), (b, e) -> b.prevLine(SelectionPolicy.ADJUST)),
      consume(anyOf(keyPressed(DOWN, SHIFT_DOWN), keyPressed(KP_DOWN, SHIFT_DOWN)), (b, e) -> b.nextLine(SelectionPolicy.ADJUST)),
      consume(keyPressed(PAGE_UP, SHIFT_DOWN), (b, e) -> b.view.prevPage(SelectionPolicy.ADJUST)),
      consume(keyPressed(PAGE_DOWN, SHIFT_DOWN), (b, e) -> b.view.nextPage(SelectionPolicy.ADJUST)));

    var otherNavigation = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent>sequence(
      // caret movement
      consume(anyOf(keyPressed(RIGHT), keyPressed(KP_RIGHT)), GenericStyledAreaBehavior::right),
      consume(anyOf(keyPressed(LEFT), keyPressed(KP_LEFT)), GenericStyledAreaBehavior::left),
      consume(keyPressed(HOME), (b, e) -> b.view.lineStart(SelectionPolicy.CLEAR)),
      consume(keyPressed(END), (b, e) -> b.view.lineEnd(SelectionPolicy.CLEAR)),
      consume(anyOf(keyPressed(RIGHT, SHORTCUT_DOWN), keyPressed(KP_RIGHT, SHORTCUT_DOWN)), (b, e) -> b.skipToNextWord(SelectionPolicy.CLEAR)),
      consume(anyOf(keyPressed(LEFT, SHORTCUT_DOWN), keyPressed(KP_LEFT, SHORTCUT_DOWN)), (b, e) -> b.skipToPrevWord(SelectionPolicy.CLEAR)),
      consume(keyPressed(HOME, SHORTCUT_DOWN), (b, e) -> b.view.start(SelectionPolicy.CLEAR)),
      consume(keyPressed(END, SHORTCUT_DOWN), (b, e) -> b.view.end(SelectionPolicy.CLEAR)),
      // selection
      consume(anyOf(keyPressed(RIGHT, SHIFT_DOWN), keyPressed(KP_RIGHT, SHIFT_DOWN)), GenericStyledAreaBehavior::selectRight),
      consume(anyOf(keyPressed(LEFT, SHIFT_DOWN), keyPressed(KP_LEFT, SHIFT_DOWN)), GenericStyledAreaBehavior::selectLeft),
      consume(keyPressed(HOME, SHIFT_DOWN), (b, e) -> b.view.lineStart(selPolicy)),
      consume(keyPressed(END, SHIFT_DOWN), (b, e) -> b.view.lineEnd(selPolicy)),
      consume(keyPressed(HOME, SHIFT_DOWN, SHORTCUT_DOWN), (b, e) -> b.view.start(selPolicy)),
      consume(keyPressed(END, SHIFT_DOWN, SHORTCUT_DOWN), (b, e) -> b.view.end(selPolicy)),
      consume(anyOf(keyPressed(RIGHT, SHIFT_DOWN, SHORTCUT_DOWN), keyPressed(KP_RIGHT, SHIFT_DOWN, SHORTCUT_DOWN)), (b, e) -> b.skipToNextWord(selPolicy)),
      consume(anyOf(keyPressed(LEFT, SHIFT_DOWN, SHORTCUT_DOWN), keyPressed(KP_LEFT, SHIFT_DOWN, SHORTCUT_DOWN)), (b, e) -> b.skipToPrevWord(selPolicy)),
      consume(keyPressed(SHORTCUT_A), (b, e) -> b.view.selectAll()));

    var copyAction = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent, KeyEvent>consume(
      anyOf(keyPressed(COPY), keyPressed(SHORTCUT_C), keyPressed(INSERT, SHORTCUT_DOWN)), (b, e) -> b.view.copy()
    );

    controlKeysFilter = e -> {
      if (isWindows) {
        //Windows input. ALT + CONTROL accelerators are the same as ALT GR accelerators.
        //If ALT + CONTROL are pressed and the given character is valid then print the character.
        //Else, don't consume the event. This change allows Windows users to use accelerators and
        //printing special characters at the same time.
        // (For example: ALT + CONTROL + E prints the euro symbol in the spanish keyboard while ALT + CONTROL + L has assigned an accelerator.)
        //Note that this is how several IDEs such JetBrains IDEs or Eclipse behave.
        if (e.isControlDown() && e.isAltDown() && !e.isMetaDown() && e.getCharacter().length() == 1 && e.getCharacter().getBytes()[0] != 0) {
          return true;
        }
        return !e.isControlDown() && !e.isAltDown() && !e.isMetaDown();
      }
      return !e.isControlDown() && !e.isMetaDown();
    };

    Predicate<KeyEvent> isChar = e -> e.getCode().isLetterKey() || e.getCode().isDigitKey() || e.getCode().isWhitespaceKey();

    var charPressConsumer = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent, KeyEvent>consume(
      keyPressed().onlyIf(isChar.and(controlKeysFilter))
    );

    // InputMapTemplate<GenericStyledAreaBehavior, ? super KeyEvent>
    var keyPressedTemplate = edits
      .orElse(otherNavigation)
      .ifConsumed((b, e) -> b.view.clearTargetCaretOffset())
      .orElse(verticalNavigation)
      .orElse(copyAction)
      .ifConsumed((b, e) -> b.view.requestFollowCaret())
       // no need to add 'ifConsumed' after charPress since
       // requestFollowCaret is called in keyTypedTemplate
      .orElse(charPressConsumer);

    var keyTypedBase = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent, KeyEvent>consume(
      // character input
      EventPattern.keyTyped().onlyIf(controlKeysFilter.and(e -> isLegal(e.getCharacter()))),
      GenericStyledAreaBehavior::keyTyped).ifConsumed((b, e) -> b.view.requestFollowCaret()
    );

    var keyTypedTemplate = InputMapTemplate.<GenericStyledAreaBehavior, KeyEvent>when(
      b -> b.view.isEditable(), keyTypedBase
    );

    var mousePressedTemplate = InputMapTemplate.<GenericStyledAreaBehavior, MouseEvent>sequence(
      // ignore mouse pressed events if the view is disabled
      process(mousePressed(MouseButton.PRIMARY), (b, e) -> b.view.isDisabled() ? Result.IGNORE : Result.PROCEED),
      // hide context menu before any other handling
      process(mousePressed(), (b, e) -> { b.view.hideContextMenu(); return Result.PROCEED; }),
      consume(mousePressed(MouseButton.PRIMARY).onlyIf(MouseEvent::isShiftDown), GenericStyledAreaBehavior::handleShiftPress),
      consume(mousePressed(MouseButton.PRIMARY).onlyIf(e -> e.getClickCount() == 1), GenericStyledAreaBehavior::handleFirstPrimaryPress),
      consume(mousePressed(MouseButton.PRIMARY).onlyIf(e -> e.getClickCount() == 2), GenericStyledAreaBehavior::handleSecondPress),
      consume(mousePressed(MouseButton.PRIMARY).onlyIf(e -> e.getClickCount() == 3), GenericStyledAreaBehavior::handleThirdPress)
    );

    Predicate<MouseEvent> primaryOnlyButton = e -> e.getButton() == MouseButton.PRIMARY && !e.isMiddleButtonDown() && !e.isSecondaryButtonDown();

    var mouseDragDetectedTemplate = InputMapTemplate.<GenericStyledAreaBehavior, MouseEvent, MouseEvent>consume(
      eventType(MouseEvent.DRAG_DETECTED).onlyIf(primaryOnlyButton), (b, e) -> b.handlePrimaryOnlyDragDetected()
    );

    var mouseDragTemplate = InputMapTemplate.<GenericStyledAreaBehavior, MouseEvent>sequence(
      process(mouseDragged().onlyIf(primaryOnlyButton), GenericStyledAreaBehavior::processPrimaryOnlyMouseDragged),
      consume(mouseDragged(), GenericStyledAreaBehavior::continueOrStopAutoScroll)
    );

    var mouseReleasedTemplate = InputMapTemplate.<GenericStyledAreaBehavior, MouseEvent>sequence(
      process(EventPattern.mouseReleased().onlyIf(primaryOnlyButton), GenericStyledAreaBehavior::processMouseReleased),
      consume(mouseReleased(), (b, e) -> b.autoscrollTo.setValue(null)) // stop auto scroll
    );

    var mouseTemplate = InputMapTemplate.<GenericStyledAreaBehavior, MouseEvent>sequence(
      mousePressedTemplate,
      mouseDragDetectedTemplate,
      mouseDragTemplate,
      mouseReleasedTemplate
    );

    var contextMenuEventTemplate = InputMapTemplate.<GenericStyledAreaBehavior, ContextMenuEvent, ContextMenuEvent>consumeWhen(
      EventPattern.eventType(ContextMenuEvent.CONTEXT_MENU_REQUESTED), b -> !b.view.isDisabled(),
      GenericStyledAreaBehavior::showContextMenu
    );

    EVENT_TEMPLATE = sequence(mouseTemplate, keyPressedTemplate, keyTypedTemplate, contextMenuEventTemplate);
  }

  /**
   * Possible dragging states.
   */
  enum DragState {
    /** No dragging is happening. */
    NO_DRAG,
    /** Mouse has been pressed inside of selected text, but drag has not been detected yet. */
    POTENTIAL_DRAG,
    /** Drag in progress. */
    DRAG,
  }

  /* ********************************************************************** *
   * Fields                                                                 *
   * ********************************************************************** */

  final GenericStyledArea<?, ?, ?> view;

  /**
   * Indicates whether an existing selection is being dragged by the user.
   */
  DragState dragSelection = DragState.NO_DRAG;

  /**
   * Indicates whether a new selection is being made by the user.
   */
  DragState dragNewSelection = DragState.NO_DRAG;

  final Var<Point2D> autoscrollTo = Var.newSimpleVar(null);

  /* ********************************************************************** *
   * Constructors                                                           *
   * ********************************************************************** */

  GenericStyledAreaBehavior(GenericStyledArea<?, ?, ?> area) {
    this.view = area;

    InputMapTemplate.installFallback(EVENT_TEMPLATE, this, b -> b.view);

    // setup auto-scroll
    var projection = Val.combine(
      autoscrollTo,
      area.layoutBoundsProperty(),
      GenericStyledAreaBehavior::project
    );
    var distance = Val.combine(
      autoscrollTo,
      projection,
      Point2D::subtract
    );
    // EventStream<Point2D>
    var deltas = nonNullValuesOf(distance)
      .emitBothOnEach(animationFrames())
      .map(t -> { // t.map((ds, nanos) -> ds.multiply(nanos / 100_000_000.0)));
        var ds = t.a();
        var nanos = t.b();
        return ds.multiply(nanos / 100_000_000.0);
       });
    valuesOf(autoscrollTo)
      .flatMap(p -> p == null ? never() /* automatically stops the scroll animation */ : deltas)
      .subscribe(ds -> {
        area.scrollBy(ds);
        projection.ifPresent(this::dragTo);
       });
  }

  /* ********************************************************************** *
   * Key handling implementation                                            *
   * ********************************************************************** */

  void keyTyped(KeyEvent event) {
    var text = event.getCharacter();
    var n = text.length();
    if (n == 0) {
      return;
    }
    view.replaceSelection(text);
  }

  void deleteBackward(KeyEvent ignore) {
    var selection = view.getSelection();
    if (selection.getLength() == 0) {
      view.deletePreviousChar();
    } else {
      view.replaceSelection("");
    }
  }

  void deleteForward(KeyEvent ignore) {
    var selection = view.getSelection();
    if (selection.getLength() == 0) {
      view.deleteNextChar();
    } else {
      view.replaceSelection("");
    }
  }

  void left(KeyEvent ignore) {
    var sel = view.getSelection();
    if (sel.getLength() == 0) {
      view.previousChar(SelectionPolicy.CLEAR);
    } else {
      view.moveTo(sel.getStart(), SelectionPolicy.CLEAR);
    }
  }

  void right(KeyEvent ignore) {
    var sel = view.getSelection();
    if (sel.getLength() == 0) {
      view.nextChar(SelectionPolicy.CLEAR);
    } else {
      view.moveTo(sel.getEnd(), SelectionPolicy.CLEAR);
    }
  }

  void selectLeft(KeyEvent ignore) {
    view.previousChar(SelectionPolicy.ADJUST);
  }

  void selectRight(KeyEvent ignore) {
    view.nextChar(SelectionPolicy.ADJUST);
  }

  void deletePrevWord(KeyEvent ignore) {
    var end = view.getCaretPosition();
    if (end > 0) {
      view.wordBreaksBackwards(2, SelectionPolicy.CLEAR);
      var start = view.getCaretPosition();
      view.replaceText(start, end, "");
    }
  }

  void deleteNextWord(KeyEvent ignore) {
    var start = view.getCaretPosition();
    if (start < view.getLength()) {
      view.wordBreaksForwards(2, SelectionPolicy.CLEAR);
      var end = view.getCaretPosition();
      view.replaceText(start, end, "");
    }
  }

  void downLines(SelectionPolicy selectionPolicy, int nLines) {
    var currentLine = view.currentLine();
    var targetLine = currentLine.offsetBy(nLines, Forward).clamp();
    if (!currentLine.sameAs(targetLine)) {
      // compute new caret position
      var hit = view.hit(view.getTargetCaretOffset(), targetLine);
      // update model
      view.moveTo(hit.getInsertionIndex(), selectionPolicy);
    }
  }

  void prevLine(SelectionPolicy selectionPolicy) {
    downLines(selectionPolicy, -1);
  }

  void nextLine(SelectionPolicy selectionPolicy) {
    downLines(selectionPolicy, 1);
  }

  void skipToPrevWord(SelectionPolicy selectionPolicy) {
    var caretPos = view.getCaretPosition();
    // if (0 == caretPos), do nothing as can't move to the left anyway
    if (1 <= caretPos) {
      var prevCharIsWhiteSpace = isWhitespace(view.getText(caretPos - 1, caretPos).charAt(0));
      view.wordBreaksBackwards(prevCharIsWhiteSpace ? 2 : 1, selectionPolicy);
    }
  }

  void skipToNextWord(SelectionPolicy selectionPolicy) {
    var caretPos = view.getCaretPosition();
    var length = view.getLength();
    // if (caretPos == length), do nothing as can't move to the right anyway
    if (caretPos <= length - 1) {
      var nextCharIsWhiteSpace = isWhitespace(view.getText(caretPos, caretPos + 1).charAt(0));
      view.wordBreaksForwards(nextCharIsWhiteSpace ? 2 : 1, selectionPolicy);
    }
  }

  /* ********************************************************************** *
   * Mouse handling implementation                                          *
   * ********************************************************************** */

  void showContextMenu(ContextMenuEvent e) {
    view.requestFocus();
    if (view.isContextMenuPresent()) {
      var menu = view.getContextMenu();
      var x = e.getScreenX() + view.getContextMenuXOffset();
      var y = e.getScreenY() + view.getContextMenuYOffset();
      menu.show(view, x, y);
    }
  }

  void handleShiftPress(MouseEvent e) {
    // ensure focus
    view.requestFocus();
    var hit = view.hit(e.getX(), e.getY());
    // On Mac always extend selection,
    // switching anchor and caret if necessary.
    view.moveTo(hit.getInsertionIndex(), isMac ? SelectionPolicy.EXTEND : SelectionPolicy.ADJUST);
  }

  void handleFirstPrimaryPress(MouseEvent e) {
    // ensure focus
    view.requestFocus();
    var hit = view.hit(e.getX(), e.getY());
    view.clearTargetCaretOffset();
    var selection = view.getSelection();
    if (view.isEditable()
        && selection.getLength() != 0
        && hit.getCharacterIndex().isPresent()
        && hit.getCharacterIndex().getAsInt() >= selection.getStart()
        && hit.getCharacterIndex().getAsInt() < selection.getEnd())
    {
      // press inside selection
      dragSelection = DragState.POTENTIAL_DRAG;
      dragNewSelection = DragState.NO_DRAG;
    } else {
      dragSelection = DragState.NO_DRAG;
      dragNewSelection = DragState.NO_DRAG;
      view.getOnOutsideSelectionMousePressed().handle(e);
    }
  }

  void handleSecondPress(MouseEvent e) {
    view.selectWord();
  }

  void handleThirdPress(MouseEvent e) {
    view.selectParagraph();
  }

  void handlePrimaryOnlyDragDetected() {
    if (dragSelection == DragState.POTENTIAL_DRAG) {
      dragSelection = DragState.DRAG;
    } else {
      dragNewSelection = DragState.DRAG;
    }
  }

  Result processPrimaryOnlyMouseDragged(MouseEvent e) {
    var p = new Point2D(e.getX(), e.getY());
    if (view.getLayoutBounds().contains(p)) {
      dragTo(p);
    }
    view.setAutoScrollOnDragDesired(true);
    // autoScrollTo will be set in "continueOrStopAutoScroll(MouseEvent)"
    return Result.PROCEED;
  }

  void continueOrStopAutoScroll(MouseEvent e) {
    if (!view.isAutoScrollOnDragDesired()) {
      autoscrollTo.setValue(null); // stops auto-scroll
    }
    var p = new Point2D(e.getX(), e.getY());
    if (view.getLayoutBounds().contains(p)) {
      autoscrollTo.setValue(null); // stops auto-scroll
    } else {
      autoscrollTo.setValue(p); // starts auto-scroll
    }
  }

  void dragTo(Point2D point) {
    if (dragSelection == DragState.DRAG || dragSelection == DragState.POTENTIAL_DRAG) { // MOUSE_DRAGGED may arrive even before DRAG_DETECTED
      view.getOnSelectionDrag().accept(point);
    } else {
      view.getOnNewSelectionDrag().accept(point);
    }
  }

  Result processMouseReleased(MouseEvent e) {
    if (view.isDisabled()) {
      return Result.IGNORE;
    }
    switch (dragSelection) {
      case POTENTIAL_DRAG:
        // selection was not dragged, but clicked
        view.getOnInsideSelectionMousePressReleased().handle(e);
        // FALL-THROUGH
      case DRAG:
        view.getOnSelectionDropped().handle(e);
        break;
      case NO_DRAG:
        if (dragNewSelection == DragState.DRAG) {
          view.getOnNewSelectionDragFinished().handle(e);
        }
        break;
    }
    dragNewSelection = DragState.NO_DRAG;
    dragSelection = DragState.NO_DRAG;
    return Result.PROCEED;
  }

  static Point2D project(Point2D p, Bounds bounds) {
    var x = clamp(p.getX(), bounds.getMinX(), bounds.getMaxX());
    var y = clamp(p.getY(), bounds.getMinY(), bounds.getMaxY());
    return new Point2D(x, y);
  }

  static double clamp(double x, double min, double max) {
    return Math.min(Math.max(x, min), max);
  }

  static boolean isControlKeyEvent(KeyEvent event) {
    return controlKeysFilter.test(event);
  }

  static boolean isLegal(String text) {
    var n = text.length();
    for (var i = 0; i < n; ++i) {
      if (Character.isISOControl(text.charAt(i))) {
        return false;
      }
    }
    return true;
  }

}
