package fx.text.junit;

import java.util.function.Function;

import fx.jupiter.FxEnv;
import fx.jupiter.FxRobot;
import fx.rich.text.InlineCssTextArea;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

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
