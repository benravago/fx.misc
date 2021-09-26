package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import fx.jupiter.FxEnv;
import fx.rich.text.NavigationActions;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static javafx.scene.input.MouseButton.PRIMARY;

import fx.text.junit.TestCase;

@Nested
class HitTests extends TestCase {

  static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
  static final String FIFTY_PARS;
  static final double PADDING_AMOUNT = 20;

  /*<init>*/ static {
    var totalPars = 50;
    var indexLimit = totalPars - 1;
    var sb = new StringBuilder();
    Consumer<Integer> appendParagraph = i -> sb.append("Par #").append(i).append(" ").append(ALPHABET);
    for (var i = 0; i < indexLimit; i++) {
      appendParagraph.accept(i);
      sb.append("\n");
    }
    appendParagraph.accept(indexLimit);
    FIFTY_PARS = sb.toString();
  }

  @BeforeEach
  void start() {
    r.interact(() -> {
      var stage = stage();
      // insure stage width doesn't change irregardless of changes in superclass' start method
      stage.setWidth(400);
      stage.setHeight(400);
    });
  }

  void moveCaretToAreaEnd() {
    area.moveTo(area.getLength());
  }

  @Nested
  class When_Area_Is_Padded {

    @Nested
    class And_Hits_Occur_Outside_Area {

      String text = "text";
      String fullText = text + "\n" + text;

      @BeforeEach
      void setup() {
        r.interact(() -> area.replaceText(fullText));
      }

      @Test @Disabled
      void clicking_in_top_padding_moves_caret_to_top_line() {
        r.interact(() -> {
          area.setPadding(new Insets(PADDING_AMOUNT, 0, 0, 0));
          moveCaretToAreaEnd();
        });
        r.root(Pos.TOP_LEFT).moveTo(1, 2).click(PRIMARY); /// moveTo(position(Pos.TOP_LEFT, 1, 2)).clickOn(PRIMARY);
        assertEquals(0, area.getCurrentParagraph());
        r.interact(() -> moveCaretToAreaEnd());
        r.root(Pos.TOP_CENTER).moveTo(0, 0).click(PRIMARY); /// moveTo(position(Pos.TOP_CENTER, 0, 0)).clickOn(PRIMARY);
        assertEquals(0, area.getCurrentParagraph());
      }

      @Test @Disabled
      void clicking_in_left_padding_moves_caret_to_beginning_of_line_on_single_line_paragraph() {
        r.interact(() -> area.setPadding(new Insets(0, 0, 0, PADDING_AMOUNT)));
        moveCaretToAreaEnd();
        r.root(Pos.TOP_LEFT).moveTo(1, 1).click(PRIMARY); /// moveTo(position(Pos.TOP_LEFT, 1, 1)).clickOn(PRIMARY);
        assertEquals(0, area.getCaretColumn());
      }

      @Test @Disabled
      void clicking_in_right_padding_moves_caret_to_end_of_line_on_single_line_paragraph() {
        r.interact(() -> {
          area.setPadding(new Insets(0, PADDING_AMOUNT, 0, 0));
          area.moveTo(0);
          // insure we're scrolled all the way to the right
          area.scrollBy(new Point2D(100, 0));
        });
        r.root(Pos.TOP_RIGHT).moveTo(-1, 1).click(PRIMARY); /// moveTo(position(Pos.TOP_RIGHT, -1, 1)).clickOn(PRIMARY);
        assertEquals(area.getParagraphLength(0), area.getCaretColumn());
      }

      @Test @Disabled
      void clicking_in_bottom_padding_moves_caret_to_bottom_line() {
        r.interact(() -> {
          area.setPadding(new Insets(0, 0, PADDING_AMOUNT, 0));
          area.moveTo(0);
          // insure we're scrolled all the way to the bottom
          area.scrollBy(new Point2D(0, 100));
        });
        r.root(Pos.BOTTOM_CENTER).moveTo(0, -2).click(PRIMARY); /// moveTo(position(Pos.BOTTOM_CENTER, 0, -2)).clickOn(PRIMARY);
        assertEquals(1, area.getCurrentParagraph());
      }

    } // And_Hits_Occur_Outside_Area

    @Nested
    class And_Hits_Occur_Inside_Area {

      @BeforeEach
      void setup() {
        r.interact(() -> {
          area.replaceText(FIFTY_PARS);
          area.setPadding(new Insets(PADDING_AMOUNT));
          area.setStyle("-fx-font-family: monospace; -fx-font-size: 12pt;");
        });
      }

      @Test
      void clicking_character_should_move_caret_to_that_position() throws InterruptedException, ExecutionException {
        assumeTrue(isHeadless());
        var start = area.getAbsolutePosition(3, 8);
        var b = FxEnv.call(() ->  area.getCharacterBoundsOnScreen(start, start + 1).get());
        r.moveTo(b).click(PRIMARY); /// moveTo(b).clickOn(PRIMARY);
        assertEquals(start, area.getCaretPosition());
      }

      @Test
      void prev_page_leaves_caret_at_bottom_of_page() {
        area.showParagraphAtBottom(area.getParagraphs().size() - 1);
        // move to last line, column 0
        area.moveTo(area.getParagraphs().size() - 1, 0);
        r.interact(() -> {
          // hit is called here
          area.prevPage(NavigationActions.SelectionPolicy.CLEAR);
        });
        assertEquals(0, area.getCaretColumn());
        assertEquals(area.lastVisibleParToAllParIndex(), area.getCurrentParagraph() + (isLinux() ? 0 : 1));
      }

      @Test
      void next_page_leaves_caret_at_top_of_page() {
        area.showParagraphAtTop(0);
        r.interact(() -> {
          area.moveTo(0);
          // hit is called here
          area.nextPage(NavigationActions.SelectionPolicy.CLEAR);
        });
        assertEquals(0, area.getCaretColumn());
        assertEquals(area.firstVisibleParToAllParIndex(), area.getCurrentParagraph() - (isLinux() ? 0 : 1));
      }

    } // And_Hits_Occur_Inside_Area

  }  // When_Area_Is_Padded

  @Nested
  class When_ParagraphBox_Is_Padded {

    @BeforeEach
    void setup() {
      r.interact(() -> {
        area.replaceText(FIFTY_PARS);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 12pt;");
        var css = HitTests.class.getResource("padded-paragraph-box.css").toExternalForm();
        area.getScene().getStylesheets().add(css);
      });
    }

    void runTest() throws InterruptedException, ExecutionException {
      var start = area.getAbsolutePosition(3, 8);
      var b = FxEnv.call(() -> area.getCharacterBoundsOnScreen(start, start + 1).get());
      r.moveTo(b).click(PRIMARY); /// moveTo(b).clickOn(PRIMARY);
      assertEquals(start, area.getCaretPosition());
    }

    @Nested
    class And_Area_Is_Padded {
      @Test
      void clicking_character_should_move_caret_to_that_position() throws InterruptedException, ExecutionException {
        assumeTrue(isHeadless());
        r.interact(() -> area.setPadding(new Insets(PADDING_AMOUNT)));
        runTest();
      }
    }

    @Nested
    class And_Area_Is_Not_Padded {
      @Test
      void clicking_character_should_move_caret_to_that_position() throws InterruptedException, ExecutionException {
        assumeTrue(isHeadless());
        runTest();
      }
    }

  } // When_ParagraphBox_Is_Padded

}
