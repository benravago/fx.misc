package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class ParagraphLinesCountTests extends TestCase {

  @Test
  void multi_line_returns_correct_count() {
    var lines = new String[] { "01 02 03 04 05", "11 12 13 14 15", "21 22 23 24 25" };
    r.interact(() -> {
      area.setWrapText(true);
      stage().setWidth(120);
      area.replaceText(String.join(" ", lines));
    });

    assertEquals(3, area.getParagraphLinesCount(0));
  }

  @Test
  void single_line_returns_one() {
    r.interact(() -> area.replaceText("some text"));
    assertFalse(area.isWrapText());

    assertEquals(1, area.getParagraphLinesCount(0));
  }

}