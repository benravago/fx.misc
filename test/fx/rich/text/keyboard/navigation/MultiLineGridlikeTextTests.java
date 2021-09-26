package fx.rich.text.keyboard.navigation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.*;

import fx.text.junit.TestCase;

@Nested
class MultiLineGridlikeTextTests extends TestCase {

  final String[] lines = {
    "01 02 03 04 05",
    "11 12 13 14 15",
    "21 22 23 24 25",
    "31 32 33 34 35",
    "41 42 43 44 45" };

  int lineStart(int lineIndex) {
    return Utils.entityStart(lineIndex, lines);
  }

  int lineEnd(int lineIndex) {
    return Utils.entityEnd(lineIndex, lines, area);
  }

  String fullText = String.join(" ", lines);

  void moveCaretTo(int position) {
    area.moveTo(position);
  }

  @BeforeEach
  void start() {
    r.interact(() -> {
      area.setWrapText(true);
      area.replaceText(fullText);
      // insures area's text appears exactly as the declaration of `lines`
      stage().setWidth(150);
      area.setStyle("-fx-font-family: monospace;" + "-fx-font-size: 12pt;");
    });
  }

  @Nested
  class When_No_Modifiers_Pressed {

    @Test
    void up_moves_caret_to_previous_line() {
      assumeTrue(isHeadless());
      moveCaretTo(lineStart(2));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(UP);
      assertEquals(lineStart(1), area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Test
    void down_moves_caret_to_next_line() {
      assumeTrue(isHeadless());
      moveCaretTo(lineStart(1));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(DOWN);
      assertEquals(lineStart(2), area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Test
    void home_moves_caret_to_start_of_current_line() {
      assumeTrue(isHeadless());
      moveCaretTo(lineEnd(1));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(HOME);
      assertEquals(lineStart(1), area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Test
    void end_moves_caret_to_end_of_current_line() {
      assumeTrue(isHeadless());
      moveCaretTo(lineStart(1));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(END);
      assertEquals(lineEnd(1), area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

  } // When_No_Modifiers_Pressed

  @Nested
  class When_Shortcut_Is_Pressed {

    @Test
    void up_does_not_move_caret() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        assertTrue(area.getSelectedText().isEmpty());
        moveCaretTo(lineStart(2));
        r.type(UP);
        assertEquals(lineStart(2), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test
    void down_does_not_move_caret() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        assertTrue(area.getSelectedText().isEmpty());
        moveCaretTo(lineStart(2));
        r.type(DOWN);
        assertEquals(lineStart(2), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void home_moves_caret_to_start_of_current_paragraph() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(lineStart(2));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(HOME);
        assertEquals(0, area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void end_moves_caret_to_end_of_current_paragraph() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(lineStart(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(END);
        assertEquals(area.getLength(), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

  } // When_Shortcut_Is_Pressed

  @Nested
  class When_Shift_Is_Pressed {

    @Test @Disabled
    void up() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(lineStart(2));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(UP);
        assertEquals(lineStart(1), area.getCaretPosition());
        assertEquals(lines[1] + " ", area.getSelectedText());
      });
    }

    @Test @Disabled
    void down() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(lineStart(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(DOWN);
        assertEquals(lineStart(2), area.getCaretPosition());
        assertEquals(lines[1] + " ", area.getSelectedText());
      });
    }

    @Test @Disabled
    void home_selects_up_to_the_start_of_current_line() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(lineEnd(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(HOME);
        assertEquals(lineStart(1), area.getCaretPosition());
        assertEquals(lines[1], area.getSelectedText());
      });
    }

    @Test @Disabled
    void end_selects_up_to_the_end_of_current_line() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(lineStart(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(END);
        assertEquals(lineEnd(1), area.getCaretPosition());
        assertEquals(lines[1], area.getSelectedText());
      });
    }

  } // When_Shift_Is_Pressed

  @Nested
  class When_Shortcut_And_Shift_Pressed {

    KeyCode[] kc = new KeyCode[] { SHORTCUT, SHIFT };

    @Test
    void up_does_not_move_caret() {
      assumeTrue(isHeadless());
      r.chord(kc, () -> {
        moveCaretTo(lineStart(2));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(UP);
        assertEquals(lineStart(2), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test
    void down_does_not_move_caret() {
      assumeTrue(isHeadless());
      r.chord(kc, () -> {
        moveCaretTo(lineStart(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(DOWN);
        assertEquals(lineStart(1), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void home_selects_up_to_the_start_of_current_paragraph() {
      assumeTrue(isHeadless());
      r.chord(kc, () -> {
        moveCaretTo(area.getLength());
        assertTrue(area.getSelectedText().isEmpty());
        r.type(HOME);
        assertEquals(0, area.getCaretPosition());
        assertEquals(area.getText(), area.getSelectedText());
      });
    }

    @Test @Disabled
    void end_selects_up_to_the_end_of_current_paragraph() {
      assumeTrue(isHeadless());
      r.chord(kc, () -> {
        moveCaretTo(0);
        assertTrue(area.getSelectedText().isEmpty());
        r.type(END);
        assertEquals(area.getLength(), area.getCaretPosition());
        assertEquals(area.getText(), area.getSelectedText());
      });
    }

  } // When_Shortcut_And_Shift_Pressed

}
