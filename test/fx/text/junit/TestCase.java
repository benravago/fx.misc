package fx.text.junit;

import java.util.function.Function;

import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import fx.rich.text.InlineCssTextArea;

import fx.jupiter.FxEnv;
import fx.jupiter.FxRobot;

public class TestCase {

  protected InlineCssTextArea area;
  protected FxRobot r;

  public TestCase() {
    r = FxEnv.robot();
    area = new InlineCssTextArea();

    r.stage(area, stage -> {
      stage.setAlwaysOnTop(true);
      stage.setWidth(400);
      stage.setHeight(400);
    });
    r.interact(() -> {
      // so tests don't need to do this themselves
      area.requestFocus();
    });
  }

  public Stage stage() {
    return (Stage) area.getScene().getWindow();
  }

  /** Builds {@code totalNumber} of lines that each have the index of the line as their text */
  public static String buildLines(int totalNumber) {
    return buildLines(totalNumber, String::valueOf);
  }

  public static String buildLines(int totalNumber, Function<Integer, String> textOnEachLine) {
    var sb = new StringBuilder();
    for (int i = 0; i < totalNumber - 1; i++) {
      sb.append(textOnEachLine.apply(i)).append("\n");
    }
    sb.append(textOnEachLine.apply(totalNumber));
    return sb.toString();
  }

  public boolean isWindows() { return System.getProperty("os.name").toLowerCase().startsWith("win"); }
  public boolean isLinux() { return System.getProperty("os.name").toLowerCase().startsWith("linux"); }
  public boolean isMac() { return System.getProperty("os.name").toLowerCase().startsWith("mac"); }

  public static boolean isHeadless() { // requires Monocle
    return "Headless".equals(System.getProperty("monocle.platform"));
  }

  public FxRobot toFirstLine() {
    return r.root(Pos.TOP_LEFT).moveTo(5, 5);
  }

  public FxRobot clickOnFirstLine(MouseButton button) {
    return toFirstLine().click(button);
  }

  public FxRobot leftClickOnFirstLine() {
    return clickOnFirstLine(MouseButton.PRIMARY);
  }
  public FxRobot rightClickOnFirstLine() {
    return clickOnFirstLine(MouseButton.SECONDARY);
  }

}

/*
 *
RichTextFXTestBase

   * Returns a specific position in the scene, starting at {@code pos} and offsetting from that place by
   * {@code xOffset} and {@code yOffset}
   *
  PointQuery position(Scene scene, Pos pos, double xOffset, double yOffset) {
    return point(scene).atPosition(pos).atOffset(xOffset, yOffset);
  }

   * Returns a specific position in the window, starting at {@code pos} and offsetting from that place by
   * {@code xOffset} and {@code yOffset}
   *
  PointQuery position(Window window, Pos pos, double xOffset, double yOffset) {
    return point(window).atPosition(pos).atOffset(xOffset, yOffset);
  }

   * Returns a specific position in the node, starting at {@code pos} and offsetting from that place by
   * {@code xOffset} and {@code yOffset}
   *
  PointQuery position(Node node, Pos pos, double xOffset, double yOffset) {
    return point(node).atPosition(pos).atOffset(xOffset, yOffset);
  }

InlineCssTextAreaAppTest

  PointQuery position(Pos pos, double xOffset, double yOffset) {
    return position(area, pos, xOffset, yOffset);
  }

  PointQuery firstLineOfArea() {
    return position(Pos.TOP_LEFT, 5, 5);
  }

  FxRobotInterface clickOnFirstLine(MouseButton... buttons) {
    return moveTo(firstLineOfArea()).clickOn(buttons);
  }

  FxRobotInterface leftClickOnFirstLine() {
    return clickOnFirstLine(MouseButton.PRIMARY);
  }

  FxRobotInterface doubleClickOnFirstLine() {
    return leftClickOnFirstLine().clickOn(MouseButton.PRIMARY);
  }

  FxRobotInterface tripleClickOnFirstLine() {
    return doubleClickOnFirstLine().clickOn(MouseButton.PRIMARY);
  }


*/
