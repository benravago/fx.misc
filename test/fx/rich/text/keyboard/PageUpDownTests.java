package fx.rich.text.keyboard;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static javafx.scene.input.KeyCode.*;

import fx.rich.text.model.ReadOnlyStyledDocument;
import fx.text.junit.TestCase;

class PageUpDownTests extends TestCase {

  static final String EIGHT_LINES = buildLines(8);

  @BeforeEach
  void start() {
    r.interact(() -> {
      r.root(); /// clickOn(area);
      // Note that these test are Font size sensitive !!!
      // allow 6 lines to be displayed
      stage().setHeight(96);
      area.replaceText(EIGHT_LINES);
    });
  }

  @Test @Disabled
  void page_up_leaves_caret_at_BOTTOM_of_viewport_when_FIRST_line_NOT_visible() {
    r.interact(() -> {
      insert(5, 1, " page_up_leaves_caret_at_BOTTOM_of_viewport");
      insert(7, 1, " page_up_leaves_caret_at_BOTTOM_of_viewport");
      area.requestFollowCaret();
    });

    var beforeBounds = area.getCaretBounds().get();

    r.type(PAGE_UP);

    var afterBounds = area.getCaretBounds().get();
    assertEquals(beforeBounds.getMinY(), afterBounds.getMinY(), 6.1);
    assertEquals(5, area.getCurrentParagraph());
  }

  @Test
  void page_up_leaves_caret_at_TOP_of_viewport_when_FIRST_line_IS_visible() {
    r.interact(() -> {
      insert(0, 1, " page_up_leaves_caret_at_TOP_of_viewport");
      area.moveTo(4, 0);
      area.requestFollowCaret();
    });

    r.type(PAGE_UP);

    assertEquals(0, area.getCurrentParagraph());
  }

  @Test @Disabled
  void page_down_leaves_caret_at_TOP_of_viewport_when_LAST_line_NOT_visible() throws Exception {
    r.interact(() -> {
      insert(0, 1, " page_down_leaves_caret_at_TOP_of_viewport");
      insert(2, 1, " page_down_leaves_caret_at_TOP_of_viewport");
      area.moveTo(0);
      area.requestFollowCaret();
    });

    var beforeBounds = area.getCaretBounds().get();

    r.type(PAGE_DOWN);

    var afterBounds = area.getCaretBounds().get();
    assertEquals(beforeBounds.getMinY(), afterBounds.getMinY(), 6.1);
    assertEquals(2, area.getCurrentParagraph());
  }

  @Test
  void page_down_leaves_caret_at_BOTTOM_of_viewport_when_LAST_line_IS_visible() throws Exception {
    r.interact(() -> {
      insert(7, 1, " page_down_leaves_caret_at_BOTTOM_of_viewport");
      area.showParagraphAtTop(3);
      area.moveTo(3, 0);
    });

    r.type(PAGE_DOWN);

    assertEquals(7, area.getCurrentParagraph());
    assertEquals(area.getLength(), area.getCaretPosition());
  }

  @Test @Disabled
  void shift_page_up_leaves_caret_at_bottom_of_viewport_and_makes_selection() {
    r.interact(() -> {
      insert(5, 1, " SHIFT_page_up_SELECTS_leaving_caret_at_BOTTOM_of_viewport");
      insert(7, 1, " SHIFT_page_up_SELECTS_leaving_caret_at_BOTTOM_of_viewport");
      area.moveTo(7, 0);
      area.requestFollowCaret();
    });

    var beforeBounds = area.getCaretBounds().get();

    r.press(SHIFT).type(PAGE_UP).release(SHIFT);

    var afterBounds = area.getCaretBounds().get();
    assertEquals(beforeBounds.getMinY(), afterBounds.getMinY(), 6.1);
    assertEquals(area.getText(5, 0, 7, 0), area.getSelectedText());
    assertEquals(10, area.getCaretPosition());
  }

  @Test @Disabled
  void shift_page_down_leaves_caret_at_top_of_viewport_and_makes_selection() {
    r.interact(() -> {
      insert(2, 1, " SHIFT_page_down_SELECTS_leaving_caret_at_TOP_of_viewport");
      insert(0, 1, " SHIFT_page_down_SELECTS_leaving_caret_at_TOP_of_viewport");
      area.requestFollowCaret();
    });

    assertTrue(area.getSelectedText().isEmpty());
    // var beforeBounds = area.getCaretBounds().get();

    r.press(SHIFT).type(PAGE_DOWN).release(SHIFT);

    // var afterBounds = area.getCaretBounds().get();
    // assertEquals(beforeBounds.getMinY(), afterBounds.getMinY(), 6.1);
    assertEquals(area.getText(1, -1, 3, -1), area.getSelectedText());
    assertEquals(119, area.getCaretPosition());
  }

  void insert(int p, int col, String text) {
    area.insert(p, col, ReadOnlyStyledDocument.fromString(text, "", "", area.getSegOps()));
  }

}
