package fx.rich.text.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import fx.jupiter.FxRobot;
import fx.text.junit.TestCase;

class ContextMenuTests extends TestCase {

  ContextMenu menu;

  // offset needs to be 5+ to prevent test failures
  double offset = 30;

  @BeforeEach
  void start() {
    r.interact(() -> {
      menu = new ContextMenu(new MenuItem("A menu item"));
      area.setContextMenu(menu);
      area.setContextMenuXOffset(offset);
      area.setContextMenuYOffset(offset);
    });
  }

  @AfterEach
  void cleanup() {
    r.interact(menu::hide);
  }

  @Test @Disabled
  void clicking_secondary_shows_context_menu() {
    // Linux passes; Mac fails; Windows untested
    //  so for now, only run on Linux
    // TODO: See if tests pass on Windows
    assumeTrue(isLinux() && isHeadless());

    // when
    rightClickOnFirstLine();

    // then
    assertTrue(area.getContextMenu().isShowing());
  }

  @Test @Disabled
  void pressing_secondary_shows_context_menu() {
    // Linux passes; Mac fails; Windows untested
    //  so for now, only run on Linux
    // TODO: See if tests pass on Windows
    assumeTrue(isLinux() && isHeadless());

    // when
    toFirstLine().press(MouseButton.SECONDARY); /// moveTo(firstLineOfArea()).press(MouseButton.SECONDARY);

    // then
    assertTrue(area.getContextMenu().isShowing());
  }

  @Test @Disabled
  void pressing_primary_mouse_button_hides_context_menu() {
    assumeTrue(isHeadless());

    // given menu is showing
    showContextMenuAt();

    toFirstLine().press(MouseButton.PRIMARY); /// moveTo(firstLineOfArea()).press(MouseButton.PRIMARY);

    assertFalse(area.getContextMenu().isShowing());
  }

  @Test @Disabled
  void pressing_middle_mouse_button_hides_context_menu() {
    assumeTrue(isHeadless());

    // given menu is showing
    showContextMenuAt();

    toFirstLine().press(MouseButton.MIDDLE); /// moveTo(firstLineOfArea()).press(MouseButton.MIDDLE);

    assertFalse(area.getContextMenu().isShowing());
  }

  @Test
  void requesting_context_nenu_via_keyboard_works_on_windows() {
    assumeTrue(isWindows() && isHeadless());

    leftClickOnFirstLine();
    r.press(KeyCode.CONTEXT_MENU);

    assertTrue(area.getContextMenu().isShowing());
  }

  void showContextMenuAt() {
    /// Point2D screenPoint = position(Pos.TOP_LEFT, offset, offset).query();
    var screenPoint = FxRobot.locate(area, Pos.TOP_LEFT).add(offset,offset);
    r.interact(() -> area.getContextMenu().show(area, screenPoint.getX(), screenPoint.getY()));
  }

}
