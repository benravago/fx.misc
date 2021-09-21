package fx.input;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.event.Event;
import javafx.scene.input.KeyEvent;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;
import static javafx.scene.input.KeyEvent.*;

import static fx.input.EventPattern.*;

class EventPatternTest {

  @Test
  void simpleKeyMatchTest() {
    // "p" prefix = EventPattern
    // "e" prefix = var

    var pAPressed = keyPressed(A);
    var pShiftAPressed = keyPressed(A, SHIFT_DOWN);
    var pAnyShiftAPressed = keyPressed(A, SHIFT_ANY);
    var pCtrlAReleased = keyReleased(A, CONTROL_DOWN);
    var pMeta_a_Typed = keyTyped("a", META_DOWN);

    var pNoControlsTyped = keyTyped().onlyIf(e -> !e.isControlDown() && !e.isAltDown() && !e.isMetaDown());
    var p_a_Typed = keyTyped("a");
    var pLeftBracketTyped = keyTypedNoMod("{");

    var eAPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, false, false);
    var eShiftAPressed = new KeyEvent(KEY_PRESSED, "", "", A, true, false, false, false);
    var eShiftAReleased = new KeyEvent(KEY_RELEASED, "", "", A, true, false, false, false);
    var eShiftMetaAPressed = new KeyEvent(KEY_PRESSED, "", "", A, true, false, false, true);
    var eCtrlAReleased = new KeyEvent(KEY_RELEASED, "", "", A, false, true, false, false);
    var eMeta_a_Typed = new KeyEvent(KEY_TYPED, "a", "", UNDEFINED, false, false, false, true);
    var eMeta_A_Typed = new KeyEvent(KEY_TYPED, "A", "", UNDEFINED, false, false, false, true);
    var eShiftQTyped = new KeyEvent(KEY_TYPED, "Q", "", UNDEFINED, true, false, false, false);
    var eQTyped = new KeyEvent(KEY_TYPED, "q", "", UNDEFINED, false, false, false, false);
    var eCtrlQTyped = new KeyEvent(KEY_TYPED, "q", "", UNDEFINED, false, true, false, false);
    var eLeftBracketTyped = new KeyEvent(KEY_TYPED, "{", "", UNDEFINED, true, false, false, true);

    var e_a_Typed = new KeyEvent(KEY_TYPED, "a", "", UNDEFINED, false, false, false, false);
    var eShift_a_Typed = new KeyEvent(KEY_TYPED, "a", "", UNDEFINED, true, false, false, false);

    assertMatchSuccess(pAPressed, eAPressed);
    assertMatchFailure(pAPressed, eShiftAPressed); // should not match when Shift pressed
    assertMatchFailure(pAPressed, eShiftMetaAPressed); // or when any other combo of modifiers pressed

    assertMatchFailure(pShiftAPressed, eAPressed); // should not match when Shift not pressed
    assertMatchSuccess(pShiftAPressed, eShiftAPressed);
    assertMatchFailure(pShiftAPressed, eShiftMetaAPressed); // should not match when Meta pressed
    assertMatchFailure(pShiftAPressed, eShiftAReleased); // released instead of pressed
    assertMatchFailure(pCtrlAReleased, eShiftAReleased); // Shift instead of Control

    assertMatchSuccess(pAnyShiftAPressed, eAPressed);
    assertMatchSuccess(pAnyShiftAPressed, eShiftAPressed);

    assertMatchSuccess(pCtrlAReleased, eCtrlAReleased);

    assertMatchSuccess(pMeta_a_Typed, eMeta_a_Typed);
    assertMatchFailure(pMeta_a_Typed, eMeta_A_Typed); // wrong capitalization

    assertMatchSuccess(pNoControlsTyped, eShiftQTyped);
    assertMatchSuccess(pNoControlsTyped, eQTyped);
    assertMatchFailure(pNoControlsTyped, eCtrlQTyped); // should not match when Control pressed

    assertMatchSuccess(pLeftBracketTyped, eLeftBracketTyped);

    // var pAltAPressed = keyPressed("a", ALT_DOWN);
    // var eAltAPressed = new KeyEvent(KEY_PRESSED, "", "", A, false, false, true, false);
    // if (!Utils.isMac()) { // https://bugs.openjdk.java.net/browse/JDK-8134723
    //  assertMatchSuccess(pAltAPressed, eAltAPressed);
    // }

    assertMatchSuccess(p_a_Typed, e_a_Typed);
    assertMatchFailure(p_a_Typed, eShift_a_Typed); // modifier is pressed
  }

  static void assertMatchSuccess(EventPattern<Event, KeyEvent> pattern, KeyEvent event) {
    assertTrue(pattern.match(event).isPresent());
  }

  static void assertMatchFailure(EventPattern<Event, KeyEvent> pattern, KeyEvent event) {
    assertFalse(pattern.match(event).isPresent());
  }

}
