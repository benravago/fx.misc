package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class MultiChangeTest extends TestCase {

  @BeforeEach
  void start() {
    // initialize area with some text
    r.interact(() -> area.replaceText("(text)") );
  }

  @Test
  void committing_single_change_works() {
    r.interact(() -> {
      var text = area.getText();
      area.createMultiChange(1)
          .deleteText(0, 1)
          .commit();

      assertEquals(text.substring(1), area.getText());
    });
  }

  @Test
  void committing_relative_change_works() {
    r.interact(() -> {
      var text = area.getText();
      var hello = "hello";
      var world = "world";
      area.createMultiChange(2)
          .insertText(0, hello)
          .insertText(0, world)
          .commit();

      assertEquals(hello + world + text, area.getText());
    });
  }

  @Test
  void committing_relative_change_backToFront_works() {
    r.interact(() -> {
      var text = area.getText();
      var hello = "hello";
      var world = "world";
      area.createMultiChange(2)
          .insertText(6, world)
          .insertText(0, hello)
          .commit();

      assertEquals(hello + text + world, area.getText());

      area.undo();
      assertEquals(text, area.getText());
    });
  }

  @Test
  void committing_absolute_change_works() {
    r.interact(() -> {
      var text = area.getText();
      var hello = "hello";
      var world = "world";
      area.createMultiChange(2)
          .insertText(0, hello)
          .insertTextAbsolutely(0, world)
          .commit();

      assertEquals(world + hello + text, area.getText());
    });
  }

  @Test
  void changing_same_content_multiple_times_works() {
    r.interact(() -> {
      var text = area.getText();
      area.createMultiChange(4)
          .replaceTextAbsolutely(0, 1, "a")
          .replaceTextAbsolutely(0, 1, "b")
          .replaceTextAbsolutely(0, 1, "c")
          .replaceTextAbsolutely(0, 1, "d")
          .commit();

      assertEquals("d" + text.substring(1), area.getText());
    });
  }

  @Test
  void attempting_to_reuse_builder_throws_exception() {
    r.interact(() -> {
      var builder = area.createMultiChange(1).insertText(0, "hey");
      builder.commit();
      assertThrows(IllegalStateException.class, () -> {
        builder.commit();
      }, "cannot reuse builder once commit changes");
    });
  }

  @Test
  void attempting_to_commit_without_any_stored_changes_throws_exception() {
    r.interact(() -> {
      var builder = area.createMultiChange(1);
      assertThrows(IllegalStateException.class, () -> {
        builder.commit();
      }, "no changes were stored in the builder");
    });
  }

}
