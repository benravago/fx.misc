package fx.rich.text.keyboard.navigation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.*;

import fx.text.junit.TestCase;

@Nested
class SingleLineTests extends TestCase {

  String[] words = { "the", "cat", "can", "walk" };
  String fullText = String.join(" ", words);

  int wordStart(int wordIndex) {
    return Utils.entityStart(wordIndex, words);
  }

  int wordEnd(int wordIndex) {
    return Utils.entityEnd(wordIndex, words, area);
  }

  void moveCaretTo(int position) {
    area.moveTo(position);
  }

  @BeforeEach
  void start() {
    r.interact(() -> {
      stage().setWidth(300);
      area.replaceText(fullText);
    });
  }

  @Nested
  class When_No_Modifiers_Pressed {

    @Test
    void left_moves_caret_one_position() {
      assumeTrue(isHeadless());
      moveCaretTo(wordStart(1));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(LEFT);
      assertEquals(wordEnd(0), area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Test
    void right_moves_caret_one_position() {
      assumeTrue(isHeadless());
      moveCaretTo(wordStart(1));
      assertTrue(area.getSelectedText().isEmpty());
      r.type(RIGHT);
      assertEquals(wordStart(1) + 1, area.getCaretPosition());
      assertTrue(area.getSelectedText().isEmpty());
    }

  } // When_No_Modifiers_Pressed

  @Nested
  class When_Shortcut_Is_Pressed {

    @Test @Disabled
    void left_once_moves_caret_to_left_boundary_of_current_word() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(wordEnd(3));
        assertTrue(area.getSelectedText().isEmpty());
        // first left goes to boundary of current word
        r.type(LEFT);
        assertEquals(wordStart(3), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void left_twice_moves_caret_to_left_boundary_of_previous_word() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(wordEnd(3));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(LEFT).type(LEFT);
        assertEquals(wordStart(2), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void right_once_moves_caret_to_right_boundary_of_current_word() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(wordStart(0));
        assertTrue(area.getSelectedText().isEmpty());
        // first right goes to boundary of current word
        r.type(RIGHT);
        assertEquals(wordEnd(0), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void right_twice_moves_caret_to_right_boundary_of_next_word() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        moveCaretTo(wordStart(0));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(RIGHT).type(RIGHT);
        assertEquals(wordEnd(1), area.getCaretPosition());
        assertTrue(area.getSelectedText().isEmpty());
      });
    }

    @Test @Disabled
    void a_selects_all() {
      assumeTrue(isHeadless());
      r.type(SHORTCUT, () -> {
        assertTrue(area.getSelectedText().isEmpty());
        r.type(A);
        assertEquals(area.getText(), area.getSelectedText());
      });
    }

  } // When_Shortcut_Is_Pressed

  @Nested
  class When_Shift_Is_Pressed {

    @Test @Disabled
    void left_selects_previous_character() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(wordStart(1));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(LEFT);
        assertEquals(wordEnd(0), area.getCaretPosition());
        assertEquals(" ", area.getSelectedText());
      });
    }

    @Test @Disabled
    void right_selects_next_character() {
      assumeTrue(isHeadless());
      r.type(SHIFT, () -> {
        moveCaretTo(wordEnd(0));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(RIGHT);
        assertEquals(wordStart(1), area.getCaretPosition());
        assertEquals(" ", area.getSelectedText());
      });
    }

  } // When_Shift_Is_Pressed

  @Nested
  class When_Shortcut_And_Shift_Pressed {

    KeyCode[] kc = { SHORTCUT, SHIFT }; /// press(SHORTCUT, SHIFT);

    @Test @Disabled
    void left_once_selects_up_to_left_boundary_of_current_word() {
      assumeTrue(isHeadless());
      r.chord(kc, () ->{
        moveCaretTo(wordEnd(3));
        assertTrue(area.getSelectedText().isEmpty());
        // first left goes to boundary of current word
        r.type(LEFT);
        assertEquals(wordStart(3), area.getCaretPosition());
        assertEquals(words[3], area.getSelectedText());
      });
    }

    @Test @Disabled
    void left_twice_selects_up_to_start_boundary_of_previous_word() {
      assumeTrue(isHeadless());
      r.chord(kc, () ->{
        moveCaretTo(wordEnd(3));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(LEFT).type(LEFT);
        assertEquals(wordStart(2), area.getCaretPosition());
        assertEquals(words[2] + " " + words[3], area.getSelectedText());
      });
    }

    @Test @Disabled
    void right_once_selects_up_to_right_boundary_of_current_word() {
      assumeTrue(isHeadless());
      r.chord(kc, () ->{
        moveCaretTo(wordStart(0));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(RIGHT);
        assertEquals(wordEnd(0), area.getCaretPosition());
        assertEquals(words[0], area.getSelectedText());
      });
    }

    @Test @Disabled
    void right_twice_selects_up_to_end_boundary_of_next_word() {
      assumeTrue(isHeadless());
      r.chord(kc, () ->{
        moveCaretTo(wordStart(0));
        assertTrue(area.getSelectedText().isEmpty());
        r.type(RIGHT).type(RIGHT);
        assertEquals(wordEnd(1), area.getCaretPosition());
        assertEquals(words[0] + " " + words[1], area.getSelectedText());
      });
    }

  } // When_Shortcut_And_Shift_Pressed

}
