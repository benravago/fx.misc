package fx.rich.text.api.caret;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.rich.text.Caret;
import fx.text.junit.TestCase;

class BoundsTests extends TestCase {

  static final String MANY_PARS_OF_TEXT = buildLines(20);

  @BeforeEach
  void start() {
    r.interact(() -> {
      stage().setHeight(50);
      // insure caret is always visible
      area.setShowCaret(Caret.CaretVisibility.ON);
      area.replaceText(MANY_PARS_OF_TEXT);
      area.moveTo(0);
      area.showParagraphAtTop(0);
    });
  }

  @Test
  void caret_bounds_are_present_after_moving_caret_and_following_it() {
    assertTrue(area.getCaretBounds().isPresent());

    // move caret outside of viewport
    r.interact(() -> {
      area.moveTo(area.getLength());
      area.requestFollowCaret();
    });

    // needed for test to pass
    r.interact(() -> {}); /// WaitForAsyncUtils.waitForFxEvents();

    // viewport should update itself so caret is visible again
    assertTrue(area.getCaretBounds().isPresent());
  }

  @Test
  void caret_bounds_are_absent_after_moving_caret_without_following_it() {
    assertTrue(area.getCaretBounds().isPresent());

    // move caret outside of viewport
    r.interact(() -> area.moveTo(area.getLength()));

    // caret should not be visible
    assertFalse(area.getCaretBounds().isPresent());
  }

}