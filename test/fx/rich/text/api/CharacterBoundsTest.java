package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class CharacterBoundsTest extends TestCase {

  @BeforeEach
  void start() {
    r.interact(() -> area.replaceText("a line of sample text"));
  }

  @Test
  void getCharacterBounds_works_even_when_a_selection_is_made() {
    area.selectAll();
    var bounds = area.getSelectionBounds().get();

    r.interact(() -> area.getCharacterBoundsOnScreen(0, area.getLength() - 1));

    assertEquals(bounds, area.getSelectionBounds().get());
  }

}
