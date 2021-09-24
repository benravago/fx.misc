package fx.rich.text.keyboard;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class TypingTests extends TestCase {

  @Test
  void typing_a_letter_moves_caret_after_the_inserted_letter() {
    r.interact(() -> {
      area.moveTo(0);
      area.clear();
    });

    var userInputtedText = "some text";
    leftClickOnFirstLine().write(userInputtedText);

    assertEquals(userInputtedText, area.getText());
    assertEquals(userInputtedText.length(), area.getCaretPosition());
    assertTrue(area.getSelectedText().isEmpty());
  }

}