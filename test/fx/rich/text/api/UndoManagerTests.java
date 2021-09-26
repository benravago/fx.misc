package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import fx.react.SuspendableYes;
import fx.rich.text.StyledTextArea;
import fx.rich.text.model.SimpleEditableStyledDocument;
import fx.rich.text.model.RichTextChange;
import fx.rich.text.model.TextChange;
import fx.rich.text.util.UndoUtils;

import fx.text.junit.TestCase;

@Nested
class UndoManagerTests {

  @Nested
  class UsingInlineCssTextArea extends TestCase {

    @Test @Disabled
    void incoming_change_is_not_merged_after_period_of_user_inactivity() {
      var text1 = "text1";
      var text2 = "text2";

      var periodOfUserInactivity = UndoUtils.DEFAULT_PREVENT_MERGE_DELAY.toMillis() + 300L;

      r.write(text1);
      r.sleep(periodOfUserInactivity);
      r.write(text2);

      r.interact(area::undo);
      assertEquals(text1, area.getText());

      r.interact(area::undo);
      assertEquals("", area.getText());
    }

    @Disabled // java.lang.IndexOutOfBoundsException: 3 not in [0, 1]
    @Test // After undo, text insertion point jumps to the start of the text area #780
          // After undo, text insertion point jumps to the end of the text area #912
    void undo_leaves_correct_insertion_point() {

      r.write("abc mno");
      r.interact(() -> {
        area.insertText(3, " def");
        area.appendText(" xyz");
      });

      assertEquals("abc def mno xyz", area.getText());

      r.interact(area::undo); // removes " xyz"
      assertEquals("abc def mno", area.getText());
      //                       ^
      assertEquals(area.getCaretPosition(), area.getSelection().getStart());
      assertEquals(11, area.getSelection().getStart());

      r.interact(area::undo); // removes " def"
      assertEquals("abc mno", area.getText());
      //               ^
      assertEquals(area.getCaretPosition(), area.getSelection().getStart());
      assertEquals(3, area.getSelection().getStart());

      r.interact(area::redo); // restore " def"
      assertEquals("abc def mno", area.getText());
      //                   ^
      assertEquals(area.getCaretPosition(), area.getSelection().getStart());
      assertEquals(7, area.getSelection().getStart());

      r.interact(area::undo); // removes " def"
      r.interact(() -> area.insertText(area.getCaretPosition(), " ?"));
      assertEquals("abc ? mno", area.getText());
    }

    @Test
    void testUndoWithWinNewlines() {
      var text1 = "abc\r\ndef";
      var text2 = "A\r\nB\r\nC";

      r.interact(() -> {
        area.replaceText(text1);
        area.getUndoManager().forgetHistory();
        area.insertText(0, text2);
        assertEquals("A\nB\nCabc\ndef", area.getText());

        area.undo();
        assertEquals("abc\ndef", area.getText());
      });
    }

    @Test
    void multiChange_undo_and_redo_works() {
      r.interact(() -> {
        var text = "text";
        var wrappedText = "(" + text + ")";
        area.replaceText(wrappedText);
        area.getUndoManager().forgetHistory();

        // Text:     |(|t|e|x|t|)|
        // Position: 0 1 2 3 4 5 6
        area.createMultiChange(2)
            // delete parenthesis
            .deleteText(0, 1)
            .deleteText(5, 6)
            .commit();

        area.undo();
        assertEquals(wrappedText, area.getText());

        area.redo();
        assertEquals(text, area.getText());
      });
    }

    @Test
    void multiChange_merge_works() {
      r.interact(() -> {
        var initialText = "123456";
        area.replaceText(initialText);
        area.getUndoManager().forgetHistory();

        var firstCount = 0;
        var secondCount = 3;

        // Text:     |1|2|3|4|5|6|
        // Position: 0 1 2 3 4 5 6
        area.createMultiChange(2)
            // replace '1' with 'a'
            .replaceText(firstCount, ++firstCount, "a")
            // replace '4' with 'c'
            .replaceText(secondCount, ++secondCount, "c").commit();

        // Text:     |a|2|3|c|5|6|
        // Position: 0 1 2 3 4 5 6
        area.createMultiChange(2)
            // replace '2' with 'b'
            .replaceText(firstCount, ++firstCount, "b")
            // replace '5' with 'd'
            .replaceText(secondCount, ++secondCount, "d").commit();

        var finalText = "ab3cd6";

        area.undo();
        assertFalse(area.getUndoManager().isUndoAvailable());
        assertEquals(initialText, area.getText());

        area.redo();
        assertFalse(area.getUndoManager().isRedoAvailable());
        assertEquals(finalText, area.getText());
      });
    }

    @Test
    void identity_change_works() {
      r.interact(() -> {
        area.replaceText("ttttt");

        var richEmissions = new SimpleIntegerProperty(0);
        var plainEmissions = new SimpleIntegerProperty(0);
        area.multiRichChanges()
            .hook(list -> richEmissions.set(richEmissions.get() + 1))
            .filter(list -> !list.stream().allMatch(TextChange::isIdentity))
            .subscribe(list -> plainEmissions.set(plainEmissions.get() + 1));

        var position = 0;
        area.createMultiChange(4)
            .replaceText(position, ++position, "t")
            .replaceText(position, ++position, "t")
            .replaceText(position, ++position, "t")
            .replaceText(position, ++position, "t")
            .commit();

        assertEquals(1, richEmissions.get());
        assertEquals(0, plainEmissions.get());
      });
    }

    @Test @Disabled
    void testForBug904() {
      var firstLine = "some text\n";
      r.write(firstLine);
      r.interact(() -> area.setStyle(5, 9, "-fx-font-weight: bold;"));
      r.write("new line");
      area.getUndoManager().preventMerge();
      r.interact(() -> area.append(area.getContent().subSequence(firstLine.length() - 1, area.getLength())));
      r.interact(area::undo); // should not throw Unexpected change received exception
    }

    @Test @Disabled // java.lang.IndexOutOfBoundsException: [5, 9) is not a valid range within [0, 0)
    void suspendable_UndoManager_skips_style_check() {

      var suspendUndo = new SuspendableYes();
      area.setUndoManager(UndoUtils.richTextSuspendableUndoManager(area, suspendUndo));
      r.write("some text\n");
      r.interact(() -> suspendUndo.suspendWhile(() -> area.setStyle(5, 9, "-fx-font-weight: bold;")));
      r.write("new line");
      r.interact(area::undo); // should not throw Unexpected change received exception

      area.setUndoManager(UndoUtils.defaultUndoManager(area));
      RichTextChange.skipStyleComparison(false);
    }

  } // UsingInlineCssTextArea

  @Nested
  class UsingStyledTextArea extends TestCase {

    @BeforeEach
    void start() {
      r.interact(() -> {
        var stage = stage();
        stage.setScene(new Scene(new Label("Ignore me..."), 400, 400));
        stage.show();
      });
    }

    @Test
    void testForBug216() {
      r.interact(() -> {
        // set up area with some styled text content
        var initialStyle = false;
        var area = new StyledTextArea<>(
          "",
          (t, s) -> {},
          initialStyle, (t, s) -> {},
          new SimpleEditableStyledDocument<>("", initialStyle),
          true
        );
        area.replaceText("testtest");
        area.setStyle(0, 8, true);

        // add a space styled by initialStyle
        area.setUseInitialStyleForInsertion(true);
        area.insertText(4, " ");

        // add another space
        area.insertText(5, " ");

        // testing that undo/redo don't throw an exception
        area.undo();
        area.redo();
      });
    }

  } // UsingStyledTextArea

}
