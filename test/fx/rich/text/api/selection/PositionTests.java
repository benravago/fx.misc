package fx.rich.text.api.selection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.rich.text.Selection;
import fx.rich.text.SelectionBase;

import fx.text.junit.TestCase;

class PositionTests extends TestCase {

  String leftText = "left";
  String rightText = "right";
  String fullText = leftText + rightText;

  Selection<String, String, String> selection;

  @BeforeEach
  void start() {
    r.interact(() -> {
      area.replaceText(fullText);
      selection = new SelectionBase<>("extra selection", area);
      area.addSelection(selection);
    });
  }

  void selectLeft() {
    selection.selectRange(0, leftText.length());
  }

  void selectRight() {
    selection.selectRange(leftText.length(), area.getLength());
  }

  @Test
  void initial_range_specified_via_constructor_is_honored() {
    r.interact(() -> {
      area.appendText("\n" + fullText);
      area.appendText("\n" + fullText);
    });

    int paragraphOne = 1;
    int start = area.getAbsolutePosition(paragraphOne, leftText.length());
    int end = start + rightText.length();

    var s0 = new SelectionBase<>("constructor", area, start, end);
    assertEquals(leftText.length(), s0.getStartColumnPosition());
    assertEquals(paragraphOne, s0.getStartParagraphIndex());

    r.interact(() -> area.replaceText(fullText));
  }

  @Test
  void start_position_is_correct_when_change_occurs_before_position() {
    r.interact(() -> {
      selectLeft();
      var pos = selection.getStartPosition();

      var append = "some";

      // add test
      area.insertText(0, append);
      assertEquals(pos + append.length(), selection.getStartPosition());

      // delete test
      area.deleteText(0, append.length());
      assertEquals(pos, selection.getStartPosition());
    });
  }

  @Test
  void start_position_is_correct_when_change_occurs_before_position_and_deletes_carets_position() {
    r.interact(() -> {
      selection.selectRange(2, leftText.length() + 1);

      area.deleteText(0, leftText.length());
      assertEquals(0, selection.getStartPosition());
    });
  }

  @Test
  void start_position_is_correct_when_change_occurs_at_position() {
    r.interact(() -> {
      selectRight();
      var pos = selection.getStartPosition();

      var append = "some";
      // add test
      area.insertText(leftText.length(), append);
      assertEquals(pos + append.length(), selection.getStartPosition());

      // reset
      selection.updateStartTo(pos);

      // delete test
      area.deleteText(pos, append.length());
      assertEquals(pos, selection.getStartPosition());
    });
  }

  @Test
  void start_position_is_correct_when_change_occurs_after_position() {
    r.interact(() -> {
      selectLeft();

      // add test
      var append = "some";
      area.appendText(append);
      assertEquals(0, selection.getStartPosition());

      // delete test
      var length = area.getLength();
      area.deleteText(length - append.length(), length);
      assertEquals(0, selection.getStartPosition());
    });
  }

  @Test
  void end_position_is_correct_when_change_occurs_before_position() {
    r.interact(() -> {
      selectRight();
      var pos = selection.getEndPosition();

      var append = "some";

      // add test
      area.insertText(0, append);
      assertEquals(pos + append.length(), selection.getEndPosition());

      // delete test
      area.deleteText(0, append.length());
      assertEquals(pos, selection.getEndPosition());
    });
  }

  @Test
  void end_position_is_correct_when_change_occurs_before_position_and_deletes_carets_position() {
    r.interact(() -> {
      selection.selectRange(leftText.length() - 1, area.getLength());

      area.deleteText(leftText.length(), area.getLength());
      assertEquals(leftText.length(), selection.getEndPosition());
    });
  }

  @Test
  void end_position_is_correct_when_change_occurs_at_position() {
    r.interact(() -> {
      selectLeft();
      var pos = selection.getEndPosition();

      var append = "some";
      // add test
      area.insertText(leftText.length(), append);
      assertEquals(pos, selection.getEndPosition());

      // delete test
      area.deleteText(pos, area.getLength());
      assertEquals(pos, selection.getEndPosition());
    });
  }

  @Test
  void end_position_is_correct_when_change_occurs_after_position() {
    r.interact(() -> {
      selectLeft();

      // add test
      var append = "some";
      area.appendText(append);
      assertEquals(leftText.length(), selection.getEndPosition());

      // delete test
      var length = area.getLength();
      area.deleteText(length - append.length(), length);
      assertEquals(leftText.length(), selection.getEndPosition());
    });
  }

  @Test
  void deletion_which_includes_selection_and_which_occurs_at_end_of_area_moves_selection_to_new_area_end() {
    r.interact(() -> {
      selection.selectRange(area.getLength(), area.getLength());
      area.deleteText(leftText.length(), area.getLength());
      assertEquals(area.getLength(), selection.getStartPosition());
      assertEquals(area.getLength(), selection.getEndPosition());
    });
  }

  @Test
  void anchor_updates_correctly_with_listener_attached() {
    r.interact(() -> {
      area.clear();
      area.anchorProperty().addListener((ob, ov, nv) -> nv++);
      area.appendText("asdf");
      area.selectRange(1, 2);
      assertEquals("s", area.getSelectedText());
      assertEquals(1, area.getAnchor());
    });
  }

}
