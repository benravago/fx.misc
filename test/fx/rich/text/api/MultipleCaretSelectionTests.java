package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.rich.text.CaretNode;
import fx.rich.text.InlineCssTextArea;

import fx.rich.text.SelectionBase;

import fx.text.junit.TestCase;

class MultipleCaretSelectionTests extends TestCase {

  @BeforeEach
  void start() {
    r.interact(() -> area.replaceText("first line\nsecond line\nthird line"));
  }

  @Test
  void adding_caret_works() {
    var caret = new CaretNode("test caret", area, 0);
    r.interact(() -> assertTrue(area.addCaret(caret)));
    assertTrue(caret.getCaretBounds().isPresent());
    assertEquals(0, caret.getPosition());
  }

  @Test
  void removing_caret_works() {
    var caret = new CaretNode("test caret", area, 0);
    r.interact(() -> {
      assertTrue(area.addCaret(caret));
      assertTrue(area.removeCaret(caret));
    });
  }

  @Test
  void adding_selection_works() {
    var selection = new SelectionBase<>("test selection", area);
    r.interact(() -> assertTrue(area.addSelection(selection)));
    // no selection made yet
    assertFalse(selection.getSelectionBounds().isPresent());
    // now bounds should be present
    r.interact(selection::selectAll);
    assertTrue(selection.getSelectionBounds().isPresent());
  }

  @Test
  void removing_selection_works() {
    var selection = new SelectionBase<>("test selection", area);
    r.interact(() -> {
      assertTrue(area.addSelection(selection));
      assertTrue(area.removeSelection(selection));
    });
  }

  @Test
  void attempting_to_remove_original_caret_fails() {
    r.interact(() -> assertFalse(area.removeCaret(area.getCaretSelectionBind().getUnderlyingCaret())));
  }

  @Test
  void attempting_to_remove_original_selection_fails() {
    r.interact(() -> assertFalse(area.removeSelection(area.getCaretSelectionBind().getUnderlyingSelection())));
  }

  @Test
  void attempting_to_add_caret_associated_with_different_area_fails() {
    var area2 = new InlineCssTextArea();
    var caret = new CaretNode("test caret", area2);
    r.interact(() -> {
      assertThrows(IllegalArgumentException.class, () -> {
        area.addCaret(caret);
      }, "cannot add a caret associated with a different area");
    });
  }

  @Test
  void attempting_to_add_selection_associated_with_different_area_fails() {
    var area2 = new InlineCssTextArea();
    var selection = new SelectionBase<>("test selection", area2);
    r.interact(() -> {
      assertThrows(IllegalArgumentException.class, () -> {
        area.addSelection(selection);
      }, "cannot add a selection associated with a different area");
    });
  }

  @Test
  void modifying_caret_before_adding_to_area_does_not_throw_exception() {
    var caret = new CaretNode("test caret", area);
    r.interact(() -> {
      caret.moveToAreaEnd();
      area.addCaret(caret);
      caret.moveToParEnd();
      area.removeCaret(caret);
      caret.moveToParStart();
      area.addCaret(caret);
      area.removeCaret(caret);
    });
  }

  @Test
  void modifying_selection_before_adding_to_area_does_not_throw_exception() {
    var selection = new SelectionBase<>("test selection", area);
    r.interact(() -> {
      selection.selectAll();
      area.addSelection(selection);
      selection.selectRange(0, 4);
      area.removeSelection(selection);
      selection.deselect();
      area.addSelection(selection);
      area.removeSelection(selection);
    });
  }

}
