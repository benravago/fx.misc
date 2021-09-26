package fx.rich.text.keyboard.navigation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javafx.scene.input.KeyCode.*;

import fx.text.junit.TestCase;

class MultiLineJaggedTextTests extends TestCase {

  /// @Rule
  /// Timeout globalTimeout = Timeout.seconds(10);

  String threeLinesOfText = "Some long amount of text to take up a lot of space in the given area.";

  @BeforeEach
  void start() {
    r.interact(() -> {
      stage().setWidth(200);
      area.replaceText(threeLinesOfText);
      area.setWrapText(true);
    });
  }

  @Test
  void pressing_down_moves_caret_to_next_line() {
    assumeTrue(isHeadless());

    area.moveTo(0);
    assertEquals(0, area.getCaretSelectionBind().getLineIndex().getAsInt());

    r.press(DOWN);

    assertEquals(1, area.getCaretSelectionBind().getLineIndex().getAsInt());
  }

  @Test
  void pressing_up_moves_caret_to_previous_line() {
    assumeTrue(isHeadless());

    area.moveTo(area.getLength());
    var lastLineIndex = area.getParagraphLinesCount(0) - 1;
    assertEquals(lastLineIndex, area.getCaretSelectionBind().getLineIndex().getAsInt());

    r.press(UP);

    assertEquals(lastLineIndex - 1, area.getCaretSelectionBind().getLineIndex().getAsInt());
  }

}
