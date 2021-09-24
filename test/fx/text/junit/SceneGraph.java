package fx.text.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import fx.layout.flow.Cell;
import fx.layout.flow.Viewport;
import fx.rich.text.BackgroundPath;
import fx.rich.text.BorderPath;
import fx.rich.text.UnderlinePath;

/**
 * Contains inspection methods to analyze the scene graph which has been rendered by RichTextFX.
 * TestFX tests should subclass this if it needs to run tests on a simple area and needs to inspect
 * whether the scene graph has been properly created.
 */
public class SceneGraph extends TestCase { // SceneGraphTests extends InlineCssTextAreaAppTest {

  /**
   * @param index The index of the desired paragraph box
   * @return The paragraph box for the paragraph at the specified index
   */
  public Region getParagraphBox(int index) {
    @SuppressWarnings("unchecked")
    var flow = (Viewport<String, Cell<String, Node>>) area
      .getChildrenUnmodifiable()
      .get(0);
    var gsa = flow
      .getCellIfVisible(index)
      .orElseThrow(() -> new IllegalArgumentException("paragraph " + index + " is not rendered on the screen"));
    // get the ParagraphBox (public subclass of Region)
    return (Region) gsa.getNode();
  }

  /**
   * @param index The index of the desired paragraph box
   * @return The ParagraphText (public subclass of TextFlow) for the paragraph at the specified index
   */
  public TextFlow getParagraphText(int index) {
    // get the ParagraphBox (public subclass of Region)
    var paragraphBox = getParagraphBox(index);
    // get the ParagraphText (public subclass of TextFlow)
    return (TextFlow) paragraphBox
      .getChildrenUnmodifiable()
      .stream()
      .filter(n -> n instanceof TextFlow)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No TextFlow node found in area at index: " + index));
  }

  /**
   * @param index The index of the desired paragraph box
   * @return A list of text nodes which render the text in the ParagraphBox
   *         specified by the given index.
   */
  public List<Text> getTextNodes(int index) {
    var tf = getParagraphText(index);
    var result = new ArrayList<Text>();
    tf.getChildrenUnmodifiable()
      .filtered(n -> n instanceof Text)
      .forEach(n -> result.add((Text) n));
    return result;
  }

  /**
   * @param index The index of the desired paragraph box
   * @return A list of nodes which render the underlines for the text in the ParagraphBox
   *         specified by the given index.
   */
  public List<Path> getUnderlinePaths(int index) {
    return getParagraphTextChildren(index, n -> n instanceof UnderlinePath, n -> (UnderlinePath) n);
  }

  public List<Path> getBorderPaths(int index) {
    return getParagraphTextChildren(index, n -> n instanceof BorderPath, n -> (BorderPath) n);
  }

  public List<Path> getBackgroundPaths(int index) {
    return getParagraphTextChildren(index, n -> n instanceof BackgroundPath, n -> (BackgroundPath) n);
  }

  <T> List<T> getParagraphTextChildren(int index, Predicate<Node> instanceOfCheck, Function<Node, T> cast) {
    var tf = getParagraphText(index);
    var result = new ArrayList<T>();
    tf.getChildrenUnmodifiable()
      .filtered(instanceOfCheck)
      .forEach(n -> result.add(cast.apply(n)));
    return result;
  }

}
