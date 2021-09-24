package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import fx.text.junit.TestCase;

class ParagraphIndexMappingTests extends TestCase {

  static final int TOTAL_NUMBER_OF_LINES = 80;
  static final int LAST_PAR_INDEX = TOTAL_NUMBER_OF_LINES - 1;
  static final String CONTENT = buildLines(TOTAL_NUMBER_OF_LINES);

  @BeforeEach
  void start() {
    r.interact(() -> area.replaceText(CONTENT));
  }

  @Test
  void all_par_to_visible_par_index_is_correct() {
    r.interact(() -> area.showParagraphAtTop(0));
    assertEquals(Optional.of(0), area.allParToVisibleParIndex(0));

    r.interact(() -> area.showParagraphAtBottom(LAST_PAR_INDEX));
    assertEquals(Optional.of(area.getVisibleParagraphs().size() - 1), area.allParToVisibleParIndex(LAST_PAR_INDEX));
  }

  @Test
  void all_par_to_visible_par_index_after_replace() {
    r.interact(() -> {
      area.clear();
      area.replaceText("123\nabc");
    });

    r.interact(() -> area.replaceText("123\nxyz"));

    r.interact(() -> {
      assertEquals(Optional.of(1), area.allParToVisibleParIndex(1));
      assertEquals(Optional.of(0), area.allParToVisibleParIndex(0));
    });
  }

  @Test
  void visible_par_to_all_par_index_is_correct() {
    r.interact(() -> area.showParagraphAtTop(0));
    assertEquals(0, area.visibleParToAllParIndex(0));

    r.interact(() -> area.showParagraphAtBottom(LAST_PAR_INDEX));
    assertEquals(LAST_PAR_INDEX, area.visibleParToAllParIndex(area.getVisibleParagraphs().size() - 1));

  }

}
