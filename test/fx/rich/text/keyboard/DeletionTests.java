package fx.rich.text.keyboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;

import fx.rich.text.NavigationActions.SelectionPolicy;

import fx.text.junit.TestCase;

@Nested
class DeletionTests {

  String text = "text";
  String text2 = join(text, text);
  String text3 = join(text2, text);

  static String join(String... strings) {
    return String.join(" ", strings);
  }

  static String withoutLastChar(String s) {
    return s.substring(0, s.length() - 1);
  }

  static String withoutFirstChar(String s) {
    return s.substring(1);
  }

  @Nested
  class When_Shortcut_Is_Down extends TestCase {

    @BeforeEach
    void start() {
      r.interact(() -> {
        area.replaceText(text3);
        r.press(KeyCode.SHORTCUT);
      });
    }

    @Test @Disabled
    void pressing_delete_removes_next_word_and_space() {
      assumeTrue(isHeadless());
      area.moveTo(0);
      var pos = area.getCaretPosition();

      r.press(KeyCode.DELETE);

      assertEquals(text2, area.getText());
      assertEquals(pos, area.getCaretPosition());
    }

    @Test @Disabled
    void pressing_backspace_removes_previous_word_and_space() {
      assumeTrue(isHeadless());
      area.end(SelectionPolicy.CLEAR);
      int pos = area.getCaretPosition();

      r.press(KeyCode.BACK_SPACE);

      assertEquals(text2, area.getText());
      assertEquals(pos - text.length() - 1, area.getCaretPosition());
    }

  } // When_Shortcut_Is_Down

  @Nested
  class When_No_Modifiers extends TestCase {

    @BeforeEach
    void start() {
      r.interact(() -> area.replaceText(text) );
    }

    @Test
    void pressing_delete_removes_next_char() {
      assumeTrue(isHeadless());
      area.moveTo(0);
      var pos = area.getCaretPosition();

      r.type(KeyCode.DELETE); /// push(DELETE);

      assertEquals(withoutFirstChar(text), area.getText());
      assertEquals(pos, area.getCaretPosition());
    }

    @Test
    void pressing_backspace_removes_previous_char() {
      assumeTrue(isHeadless());
      area.end(SelectionPolicy.CLEAR);
      var pos = area.getCaretPosition();

      r.type(KeyCode.BACK_SPACE); /// push(DELETE);

      assertEquals(withoutLastChar(text), area.getText());
      assertEquals(pos - 1, area.getCaretPosition());
    }

  } // When_No_Modifiers

  // miscellaneous cases

  @Nested
  class When_Area_Ends_With_Empty_Line extends TestCase {

    @BeforeEach
    void start() {
      r.interact(() -> area.replaceText(0, 0, "abc\n") );
    }

    @Nested
    class And_All_Text_Is_Selected {

      @BeforeEach
      void selectAllText() {
        assumeTrue(isHeadless());
        r.interact(() -> area.selectAll());
      }

      @Test
      void pressing_delete_should_not_throw_exception() {
        assumeTrue(isHeadless());
        r.type(KeyCode.DELETE); /// push(DELETE);
      }

      @Test
      void pressing_backspace_should_not_throw_exceptions() {
        assumeTrue(isHeadless());
        r.type(KeyCode.BACK_SPACE); /// push(BACK_SPACE);
      }

    } // And_All_Text_Is_Selected
  } // When_Area_Ends_With_Empty_Line

}
