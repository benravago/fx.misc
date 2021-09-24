package fx.rich.text.style;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.rich.text.model.StyleSpansBuilder;
import fx.text.junit.SceneGraph;

class StylingTests extends SceneGraph {

  final static String HELLO = "Hello ";
  final static String WORLD = "World";
  final static String AND_ALSO_THE = " and also the ";
  final static String SUN = "Sun";
  final static String AND_MOON = " and Moon";

  @Test
  void simpleStyling() {
    // setup
    r.interact(() -> {
      area.replaceText(HELLO + WORLD + AND_MOON);
    });

    // expected: one text node which contains the complete text
    var textNodes = getTextNodes(0);
    assertEquals(1, textNodes.size());

    r.interact(() -> {
      area.setStyle(HELLO.length(), HELLO.length() + WORLD.length(), "-fx-font-weight: bold;");
    });

    // expected: three text nodes
    textNodes = getTextNodes(0);
    assertEquals(3, textNodes.size());

    var first = textNodes.get(0);
    assertEquals("Hello ", first.getText());
    assertEquals("Regular", first.getFont().getStyle());

    var second = textNodes.get(1);
    assertEquals("World", second.getText());
    assertEquals("Bold", second.getFont().getStyle());

    var third = textNodes.get(2);
    assertEquals(" and Moon", third.getText());
    assertEquals("Regular", third.getFont().getStyle());
  }

  @Test
  void underlineStyling() {

    var underlineStyle =
      "-rtfx-underline-color: red; -rtfx-underline-dash-array: 2 2; -rtfx-underline-width: 1; -rtfx-underline-cap: butt;";

    // setup
    r.interact(() -> {
      area.replaceText(HELLO + WORLD + AND_ALSO_THE + SUN + AND_MOON);
    });

    // expected: one text node which contains the complete text
    var textNodes = getTextNodes(0);
    assertEquals(1, textNodes.size());
    assertEquals(HELLO + WORLD + AND_ALSO_THE + SUN + AND_MOON, textNodes.get(0).getText());

    r.interact(() -> {
      var start1 = HELLO.length();
      var end1 = start1 + WORLD.length();
      area.setStyle(start1, end1, underlineStyle);

      var start2 = end1 + AND_ALSO_THE.length();
      var end2 = start2 + SUN.length();
      area.setStyle(start2, end2, underlineStyle);
    });

    // expected: five text nodes
    textNodes = getTextNodes(0);
    assertEquals(5, textNodes.size());

    var first = textNodes.get(0);
    assertEquals(HELLO, first.getText());
    var second = textNodes.get(1);
    assertEquals(WORLD, second.getText());
    var third = textNodes.get(2);
    assertEquals(AND_ALSO_THE, third.getText());
    var fourth = textNodes.get(3);
    assertEquals(SUN, fourth.getText());
    var fifth = textNodes.get(4);
    assertEquals(AND_MOON, fifth.getText());

    // determine the underline paths - need to be two of them!
    var underlineNodes = getUnderlinePaths(0);
    assertEquals(2, underlineNodes.size());
  }

  @Test
  void consecutive_border_styles_that_are_the_same_are_rendered_with_one_shape() {
    var boxed =
      "-rtfx-border-stroke-width: .75pt;" +
      "-rtfx-border-stroke-type: outside;" +
      "-rtfx-border-stroke-color: darkgoldenrod;" +
      "-rtfx-background-color: antiquewhite;";

    var other =
      "-fx-font-weight: bolder;" +
      "-fx-fill: blue;";

    var text = "Lorem ipsum dolor sit amet consectetuer adipiscing elit";
    r.interact(() -> area.replaceText(text));

    // split the text up for easier style adding using String#length
    var first = text.substring(0, "Lorem ".length());
    var offset = first.length();
    var second = text.substring(offset, offset + "ipsum dolo".length());
    offset += second.length();
    var third = text.substring(offset, offset + "r sit amet".length());
    offset += third.length();
    var fourth = text.substring(offset);

    // create the styleSpans object with overlayed styles at the second span
    var builder = new StyleSpansBuilder<String>();
    builder.add(boxed, first.length());
    builder.add(boxed + other, second.length());
    builder.add(boxed, third.length());
    builder.add("", fourth.length());
    r.interact(() -> area.setStyleSpans(0, builder.create()));

    // end result: 1 path should be used for Boxed, not three
    // Text:  "Lorem ipsum dolor sit amet consectetuer adipiscing elit
    // Boxed: |**************************|
    // Other:       |**********|

    assertEquals(1, getBorderPaths(0).size());
  }

  @Test
  void consecutive_underline_styles_that_are_the_same_are_rendered_with_one_shape() {
    var underline =
      "-rtfx-underline-width: .75pt;" +
      "-rtfx-underline-color: red;" +
      "-rtfx-underline-dash-array: 2 2;";

    var other =
      "-fx-font-weight: bolder;" +
      "-fx-fill: blue;";

    var text = "Lorem ipsum dolor sit amet consectetuer adipiscing elit";
    r.interact(() -> area.replaceText(text));

    // split the text up for easier style adding using String#length
    var first = text.substring(0, "Lorem ".length());
    var offset = first.length();
    var second = text.substring(offset, offset + "ipsum dolo".length());
    offset += second.length();
    var third = text.substring(offset, offset + "r sit amet".length());
    offset += third.length();
    var fourth = text.substring(offset);

    // create the styleSpans object with overlayed styles at the second span
    var builder = new StyleSpansBuilder<String>();
    builder.add(underline, first.length());
    builder.add(underline + other, second.length());
    builder.add(underline, third.length());
    builder.add("", fourth.length());
    r.interact(() -> area.setStyleSpans(0, builder.create()));

    // end result: 1 path should be used for Underline, not three
    // Text:      "Lorem ipsum dolor sit amet consectetuer adipiscing elit
    // Underline: |**************************|
    // Other:           |**********|

    assertEquals(1, getUnderlinePaths(0).size());
  }

  @Test
  void unconsecutive_background_styles_should_each_be_rendered_with_their_own_shape() {
    var style = "-rtfx-background-color: #ccc;";
    r.interact(() -> {
      // Text:  |aba abba|
      // Style: |x x x  x|

      area.replaceText("aba abba");
      area.setStyle(0, 1, style);
      area.setStyle(2, 3, style);
      area.setStyle(4, 5, style);
      area.setStyle(7, 8, style);
    });
    var paths = getBackgroundPaths(0);
    assertEquals(4, paths.size());
  }

}
