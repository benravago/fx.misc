package fx.rich.text.api.caret;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.rich.text.CaretNode;
import fx.text.junit.TestCase;

class PositionTests extends TestCase {

  CaretNode caret;
  String text = "text";

  @BeforeEach
  void start() {
    r.interact(() -> {
      area.replaceText(text);
      caret = new CaretNode("extra caret", area);
      area.addCaret(caret);
    });
  }

  @Test
  void position_is_correct_when_change_occurs_before_position() {
    r.interact(() -> {
      caret.moveToAreaEnd();
      var pos = caret.getPosition();

      var append = "some";

      // add test
      area.insertText(0, append);
      assertEquals(pos + append.length(), caret.getPosition());

      // delete test
      area.deleteText(0, append.length());
      assertEquals(pos, caret.getPosition());
    });
  }

  @Test
  void position_is_correct_when_change_occurs_before_position_and_deletes_carets_position() {
    r.interact(() -> {
      caret.moveTo(text.length() - 1);

      area.appendText("append");
      area.deleteText(0, text.length());
      assertEquals(0, caret.getPosition());
    });
  }

  @Test
  void position_is_correct_when_change_occurs_at_position() {
    r.interact(() -> {
      caret.moveToAreaEnd();
      var pos = caret.getPosition();

      var append = "some";
      // add test
      area.appendText(append);
      assertEquals(pos + append.length(), caret.getPosition());

      // reset
      caret.moveTo(pos);

      // delete test
      area.deleteText(pos, area.getLength());
      assertEquals(pos, caret.getPosition());
    });
  }

  @Test
  void position_is_correct_when_change_occurs_after_position() {
    r.interact(() -> {
      caret.moveTo(0);

      // add test
      var append = "some";
      area.appendText(append);
      assertEquals(0, caret.getPosition());

      // delete test
      var length = area.getLength();
      area.deleteText(length - append.length(), length);
      assertEquals(0, caret.getPosition());
    });
  }

}