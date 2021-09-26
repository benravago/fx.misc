package fx.rich.text.keyboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import fx.rich.text.CodeArea;
import fx.layout.flow.VirtualizedScrollPane;

import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.*;

import fx.text.junit.TestCase;

@Nested
class CutCopyPasteTests extends TestCase {

  String text = "text";

  String beginning = "Once upon a time, ";
  String middle = "a princess was saved";
  String end = " by a knight.";

  String fullText = beginning + middle + end;

  static KeyCode[] press(KeyCode... a) { return a; }

  @BeforeEach
  void start() {
    r.interact(() -> area.replaceText(fullText));
  }

  @Nested
  class When_Nothing_Is_Selected {

    @BeforeEach
    void insureSelectionIsEmpty() {
      area.moveTo(beginning.length());
      assertTrue(area.getSelectedText().isEmpty());
    }

    @Nested
    class Nothing_Is_Stored_In_Clipboard_When_Copy_Via {

      void runAssert() {
        r.interact(() -> assertFalse(Clipboard.getSystemClipboard().hasString()));
      }

      @BeforeEach
      void insureClipboardHasNoContent() {
        r.interact(() -> Clipboard.getSystemClipboard().clear());
      }

      @Test
      void copy() {
        assumeFalse(isWindows()); // USING_AWT_ADAPTER
        r.chord(press(COPY), this::runAssert);
      }

      @Test
      void shortcut_c() {
        r.chord(press(SHORTCUT, C), this::runAssert);
      }

      @Test
      void shortcut_insert() {
        r.chord(press(SHORTCUT, INSERT), this::runAssert);
      }

    } // Nothing_Is_Stored_In_Clipboard_When_Copy_Via

    @Nested
    class Nothing_Is_Removed_In_Area_When_Cut_Via {

      void runAssert() {
        assertEquals(fullText, area.getText());
      }

      @Test
      void cut() {
        assumeFalse(isWindows()); // USING_AWT_ADAPTER
        r.chord(press(CUT), this::runAssert);
      }

      @Test
      void shortcut_x() {
        r.chord(press(SHORTCUT, X), this::runAssert);
      }

      @Test
      void shift_delete() {
        r.chord(press(SHIFT, DELETE), this::runAssert);
      }

    } // Nothing_Is_Removed_In_Area_When_Cut_Via

    @Nested
    class Text_Is_Inserted_In_Area_When_Paste_Via {

      void runAssert() {
        assertEquals(beginning + text + middle + end, area.getText());
      }

      @BeforeEach
      void storeTextInClipboard() {
        r.interact(() -> {
          var content = new ClipboardContent();
          content.putString(text);
          Clipboard.getSystemClipboard().setContent(content);
        });
      }

      @Test
      void paste() {
        // this test fails on Linux; Windows is untested
        // so for now, only run on Mac
        // TODO: update if test succeeds on Windows, too
        assumeTrue(isMac());
        r.type(PASTE, this::runAssert);
      }

      @Test @Disabled
      void shortcut_v() {
        r.chord(press(SHORTCUT, V), this::runAssert);
      }

      @Test @Disabled
      void shift_insert() {
        r.chord(press(SHIFT, INSERT), this::runAssert);
      }

    } // Text_Is_Inserted_In_Area_When_Paste_Via

  } // When_Nothing_Is_Selected

  @Nested
  class When_Text_Is_Selected {

    int startMiddle = beginning.length();
    int endMiddle = startMiddle + middle.length();

    @BeforeEach
    void selectMiddleAndClearClipboard() {
      area.selectRange(startMiddle, endMiddle);
      assertEquals(middle, area.getSelectedText());
      r.interact(() -> Clipboard.getSystemClipboard().clear());
    }

    @Nested
    class Selection_Is_Stored_In_Clipboard_When_Copy_Via {

      void runAssert() {
        r.interact(() -> {
          assertTrue(Clipboard.getSystemClipboard().hasString());
          assertEquals(middle, Clipboard.getSystemClipboard().getString());
        });
      }

      @Test
      void copy() {
        // this test fails on Linux; Windows is untested
        // so for now, only run on Mac
        // TODO: update if test succeeds on Windows, too
        assumeTrue(isMac());
        r.type(COPY, this::runAssert);
      }

      @Test @Disabled
      void shortcut_c() {
        r.chord(press(SHORTCUT, C), this::runAssert);
      }

      @Test @Disabled
      void shortcut_insert() {
        r.chord(press(SHORTCUT, INSERT), this::runAssert);
      }

    } // Selection_Is_Stored_In_Clipboard_When_Copy_Via

    @Nested
    class Selection_Is_Removed_And_Stored_In_Clipboard_When_Cut_Via {

      void runAssert() {
        assertEquals(beginning + end, area.getText());
        r.interact(() -> {
          assertTrue(Clipboard.getSystemClipboard().hasString());
          assertEquals(middle, Clipboard.getSystemClipboard().getString());
        });
      }

      @Test
      void cut() {
        // this test fails on Linux; Windows is untested
        // so for now, only run on Mac
        // TODO: update if test succeeds on Windows, too
        assumeTrue(isMac());
        r.type(CUT, this::runAssert);
      }

      @Test @Disabled
      void shortcut_x() {
        r.chord(press(SHORTCUT, X), this::runAssert);
      }

      @Test @Disabled
      void shift_delete() {
        r.chord(press(SHIFT, DELETE), this::runAssert);
      }

    } // Selection_Is_Removed_And_Stored_In_Clipboard_When_Cut_Via {

    @Nested
    class Selection_Is_Replaced_In_Area_When_Paste_Via {

      @BeforeEach
      void storeTextInClipboard() {
        r.interact(() -> {
          var content = new ClipboardContent();
          content.putString(text);
          Clipboard.getSystemClipboard().setContent(content);
        });
      }

      void runAssert() {
        assertEquals(beginning + text + end, area.getText());
      }

      @Test
      void paste() {
        // this test fails on Linux; Windows is untested
        // so for now, only run on Mac
        // TODO: update if test succeeds on Windows, too
        assumeTrue(isMac());
        r.type(PASTE, this::runAssert);
      }

      @Test @Disabled
      void shortcut_v() {
        r.chord(press(SHORTCUT, V), this::runAssert);
      }

      @Test @Disabled
      void shift_insert() {
        r.chord(press(SHIFT, INSERT), this::runAssert);
      }

    } // Selection_Is_Replaced_In_Area_When_Paste_Via

  } // When_Text_Is_Selected

  @Nested
  class MiscellaneousCases  {

    CodeArea area;

    @BeforeEach
    void start() {
      r.interact(() -> {
        area = new CodeArea("abc\ndef\nghi");
        var vsPane = new VirtualizedScrollPane<CodeArea>(area);
        var primaryStage = stage();
        var scene = new Scene(vsPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
      });
    }

    @Nested
    class When_User_Makes_Selection_Ending_In_Newline_Character {

      @BeforeEach
      void setup() {
        r.interact(() -> area.selectRange(2, 4));
      }

      @Test
      void copying_and_pasting_should_not_throw_exception() {
        r.interact(area::copy);
        r.interact(area::paste);
      }

    } // When_User_Makes_Selection_Ending_In_Newline_Character

  } // MiscellaneousCases

}
