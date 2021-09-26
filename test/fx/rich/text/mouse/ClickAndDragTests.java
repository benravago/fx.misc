package fx.rich.text.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.MouseButton.*;

import fx.jupiter.FxEnv;
import fx.text.junit.TestCase;

@Nested
class ClickAndDragTests {

  @Nested
  class When_Area_Is_Disabled extends TestCase {

    @BeforeEach
    void start() {
      r.interact(() -> {
        area.setDisable(true);
        area.replaceText("When Area Is Disabled Test: Some text goes here");
        area.moveTo(0);
      });
    }

    @Test
    void shift_clicking_area_does_nothing() {
      toFirstLine().moveTo(20, 0).press(SHIFT).press(PRIMARY); /// moveTo(firstLineOfArea()).moveBy(20, 0).press(SHIFT).press(PRIMARY);
      assertFalse(area.isFocused());
    }

    @Test
    void single_clicking_area_does_nothing() {
      leftClickOnFirstLine();
      assertFalse(area.isFocused());
    }

    @Test
    void double_clicking_area_does_nothing() {
      toFirstLine().click(PRIMARY,2); /// doubleClickOnFirstLine();
      assertFalse(area.isFocused());
    }

    @Test
    void triple_clicking_area_does_nothing() {
      toFirstLine().click(PRIMARY,3); /// tripleClickOnFirstLine();
      assertFalse(area.isFocused());
    }

    @Test
    void dragging_the_mouse_does_not_select_text() {
      toFirstLine().press(PRIMARY).moveTo(20, 0); /// moveTo(firstLineOfArea()).press(PRIMARY).moveBy(20, 0);
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Test @Disabled // TODO: implement dropBy()
    void releasing_the_mouse_after_drag_does_nothing() {
      assertEquals(0, area.getCaretPosition());
      var i = new SimpleIntegerProperty(0);
      area.setOnNewSelectionDragFinished(e -> i.set(1));
      /// moveTo(firstLineOfArea()).press(PRIMARY).dropBy(20, 0);
      assertEquals(0, area.getCaretPosition());
      assertEquals(0, i.get());
    }

  } // When_Area_Is_Disabled

  @Nested
  class When_Area_Is_Enabled {

    @Nested
    class And_Text_Is_Not_Selected extends TestCase {

      String firstWord = "Some";
      String firstParagraph = firstWord + " text goes here";
      String secondWord = "More";
      String secondParagraph = secondWord + " text goes here";

      @BeforeEach
      void start() {
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + secondParagraph);
          area.moveTo(0);
        });
      }

      @Test @Disabled
      void single_clicking_area_moves_caret_to_that_position() throws InterruptedException, ExecutionException {
        assertEquals(0, area.getCaretPosition());
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1).get());
        r.moveTo(bounds).click(PRIMARY);
        assertEquals(firstWord.length(), area.getCaretPosition());
      }

      @Test
      void single_clicking_area_beyond_text_moves_caret_to_end_position() throws InterruptedException, ExecutionException {
        var position = firstParagraph.length() + secondWord.length();
        var css = ClickAndDragTests.class.getResource("padtest.css").toExternalForm();
        r.interact(() -> area.getStylesheets().add(css));
        assertEquals(0, area.getCaretPosition());
        r.from(area).click(PRIMARY); /// clickOn(area);
        var b = FxEnv.call(() -> area.getCharacterBoundsOnScreen(position, position + 1).get());
        r.moveTo(new Point2D(b.getMaxX(), b.getMaxY() + 25)).click(PRIMARY);
        assertEquals(area.getLength(), area.getCaretPosition());
      }

      @Test @Disabled
      void double_clicking_text_in_area_selects_closest_word() {
        toFirstLine().click(PRIMARY,2); /// doubleClickOnFirstLine();
        assertEquals(firstWord, area.getSelectedText());
      }

      @Test @Disabled
      void triple_clicking_line_in_area_selects_paragraph() throws InterruptedException, ExecutionException {
        var wordStart = firstParagraph.length() + 1;
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(wordStart, wordStart + 1).get());
        r.moveTo(bounds).click(PRIMARY,2).click(PRIMARY); /// moveTo(bounds).doubleClickOn(PRIMARY).clickOn(PRIMARY);
        assertEquals(secondParagraph, area.getSelectedText());
      }

      @Test @Disabled
      void pressing_mouse_over_text_and_dragging_mouse_selects_text() {
        toFirstLine().press(PRIMARY).moveTo(20,0); /// moveTo(firstLineOfArea()).press(PRIMARY).moveBy(20, 0);
        assertFalse(area.getSelectedText().isEmpty());
      }

      @Test @Disabled
      void pressing_mouse_over_text_and_dragging_and_releasing_mouse_triggers_new_selection_finished() {
        // Doesn't work on Mac builds; works on Linux & Windows
        assumeFalse(isMac());

        var i = new SimpleIntegerProperty(0);
        area.setOnNewSelectionDragFinished(e -> i.set(1));
        toFirstLine().press(PRIMARY).moveTo(20,0).release(PRIMARY); /// moveTo(firstLineOfArea()).press(PRIMARY).moveBy(20, 0).release(PRIMARY);
        assertFalse(area.getSelectedText().isEmpty());
        assertEquals(1, i.get());
      }

    } // And_Text_Is_Not_Selected

    @Nested
    class And_Text_Is_Selected extends TestCase {

      String firstWord = "Some";
      String firstParagraph = firstWord + " text goes here";
      String extraText = "This is extra text";

      @Test
      void single_clicking_within_selected_text_moves_caret_to_that_position() throws InterruptedException, ExecutionException {
        assumeTrue(isHeadless());
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph);
          area.selectAll();
        });
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1).get());
        r.moveTo(bounds).click(PRIMARY);
        assertEquals(firstWord.length(), area.getCaretPosition());
      }

      @Test @Disabled
      void double_clicking_within_selected_text_selects_closest_word() {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph);
          area.selectAll();
        });
        toFirstLine().click(PRIMARY,2); /// doubleClickOnFirstLine();
        assertEquals(firstWord, area.getSelectedText());
      }

      @Test
      void triple_clicking_within_selected_text_selects_paragraph() {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph);
          area.selectAll();
        });
        toFirstLine().click(PRIMARY,3); /// tripleClickOnFirstLine();
        assertEquals(firstParagraph, area.getSelectedText());
      }

      @Test
      void single_clicking_within_selected_text_does_not_trigger_new_selection_finished() throws InterruptedException, ExecutionException {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph);
          area.selectAll();
        });
        var i = new SimpleIntegerProperty(0);
        area.setOnNewSelectionDragFinished(e -> i.set(1));
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1).get());
        r.moveTo(bounds).click(PRIMARY);
        assertEquals(0, i.get());
      }

      @Test
      void single_clicking_outside_of_selected_text_moves_caret_to_that_position() throws InterruptedException, ExecutionException {
        assumeTrue(isHeadless());
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + "this is the selected text");
          area.selectRange(1, 0, 2, -1);
        });
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1).get());
        r.moveTo(bounds).click(PRIMARY);
        assertEquals(firstWord.length(), area.getCaretPosition());
      }

      @Test @Disabled
      void double_clicking_outside_of_selected_text_selects_closest_word() {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + "this is the selected text");
          area.selectRange(1, 0, 2, -1);
        });
        toFirstLine().click(PRIMARY,2); /// doubleClickOnFirstLine();
        assertEquals(firstWord, area.getSelectedText());
      }

      @Test @Disabled
      void triple_clicking_outside_of_selected_text_selects_paragraph() {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + "this is the selected text");
          area.selectRange(1, 0, 2, -1);
        });
        toFirstLine().click(PRIMARY,3); /// tripleClickOnFirstLine();
        assertEquals(firstParagraph, area.getSelectedText());
      }

      @Test
      void single_clicking_outside_of_selected_text_does_not_trigger_new_selection_finished() throws InterruptedException, ExecutionException {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + "this is the selected text");
          area.selectRange(1, 0, 2, -1);
        });
        var i = new SimpleIntegerProperty(0);
        area.setOnNewSelectionDragFinished(e -> i.set(1));
        var bounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1).get());
        r.moveTo(bounds).click(PRIMARY);
        assertEquals(0, i.get());
      }

      @Test @Disabled
      void pressing_mouse_on_unselected_text_and_dragging_makes_new_selection() {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + "this is the selected text");
          area.selectRange(1, 0, 2, -1);
        });
        var originalSelection = area.getSelectedText();
        toFirstLine().press(PRIMARY).moveTo(20, 0);         /// moveTo(firstLineOfArea()).press(PRIMARY).moveBy(20, 0);
        assertFalse(originalSelection.equals(area.getSelectedText()));
      }

      @Test @Disabled
      void pressing_mouse_on_selection_and_dragging_displaces_caret() throws InterruptedException, ExecutionException {
        // setup
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + extraText);
          area.selectRange(0, firstParagraph.length());
        });
        var selText = area.getSelectedText();
        var firstLetterBounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(1, 2)).get();
        var firstWordEndBounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(firstWord.length(), firstWord.length() + 1)).get();
        r.moveTo(firstLetterBounds).press(PRIMARY).moveTo(firstWordEndBounds);
        assertEquals(firstWord.length(), area.getCaretPosition());
        assertEquals(selText, area.getSelectedText());
      }

      @Test @Disabled // implement dropTo()
      void pressing_mouse_on_selection_and_dragging_and_releasing_moves_selected_text_to_that_position() throws InterruptedException, ExecutionException {
        // Linux passes; Mac fails at "assertEquals(selText, area.getSelectedText())"; Windows is untested
        // so only run on Linux
        // TODO: update test to see if it works on Windows
        assumeTrue(isLinux());

        // setup
        var twoSpaces = "  ";
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + twoSpaces + extraText);
          area.selectRange(0, firstWord.length());
        });
        var selText = area.getSelectedText();
        var letterInFirstWord = FxEnv.call(() -> area.getCharacterBoundsOnScreen(1, 2)).get();
        var insertionPosition = firstParagraph.length() + 2;
        var insertionBounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(insertionPosition, insertionPosition + 1)).get();
        /// moveTo(letterInFirstWord).press(PRIMARY).dropTo(insertionBounds);
        var expectedText = firstParagraph.substring(firstWord.length()) + "\n" + " " + firstWord + " " + extraText;
        assertEquals(insertionPosition, area.getCaretPosition());
        assertEquals(selText, area.getSelectedText());
        assertEquals(expectedText, area.getText());
      }

      @Test @Disabled // implement dropTo()
      void pressing_mouse_on_selection_and_dragging_and_releasing_does_not_trigger_new_selection_finished() throws InterruptedException, ExecutionException {
        // Linux passes; Mac/Windows uncertain
        // TODO: update test to see if it works on Mac & Windows
        assumeTrue(isLinux());

        // setup
        var twoSpaces = "  ";
        r.interact(() -> {
          area.replaceText(firstParagraph + "\n" + twoSpaces + extraText);
          area.selectRange(0, firstWord.length());
        });
        var i = new SimpleIntegerProperty(0);
        area.setOnNewSelectionDragFinished(e -> i.set(1));
        var letterInFirstWord = FxEnv.call(() -> area.getCharacterBoundsOnScreen(1, 2)).get();
        var insertionPosition = firstParagraph.length() + 2;
        var insertionBounds = FxEnv.call(() -> area.getCharacterBoundsOnScreen(insertionPosition, insertionPosition + 1)).get();
        /// moveTo(letterInFirstWord).press(PRIMARY).dropTo(insertionBounds);
        assertEquals(0, i.get());
      }

    } // And_Text_Is_Selected

  } // When_Area_Is_Enabled

}
