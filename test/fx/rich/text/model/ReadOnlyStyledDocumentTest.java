package fx.rich.text.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static fx.rich.text.model.ReadOnlyStyledDocument.*;

class ReadOnlyStyledDocumentTest {

  static Void NULL = new Void();

  static class Void {} // Short for Void
  // cannot pass in 'null' since compiler will interpret it as a StyleSpans argument to Paragraph's constructor

  @Test
  void testUndo() {
    var segOps = SegmentOps.<String>styledTextOps();
    var doc0 = fromString("", "X", "X", segOps);

    doc0
      .replace(0, 0, fromString("abcd", "Y", "Y", segOps))
      .exec((doc1, chng1, pchng1) -> {
        // undo chng1
        doc1
          .replace(chng1.getPosition(), chng1.getInsertionEnd(), from(chng1.getRemoved()))
          .exec((doc2, chng2, pchng2) -> {
            // we should have arrived at the original document
            assertEquals(doc0, doc2);
            // chng2 should be the inverse of chng1
            assertEquals(chng1.invert(), chng2);
           });
       });
  }

  @Test
  void testMultiParagraphFromSegment() {
    var segOps = SegmentOps.<Void>styledTextOps();
    var doc0 = fromSegment("Foo\nBar", NULL, NULL, segOps);
    assertEquals(2, doc0.getParagraphCount());
  }

  @Test
  void deleteNewlineTest() {
    var segOps = SegmentOps.<Void>styledTextOps();
    var doc0 = fromString("Foo\nBar", NULL, NULL, segOps);
    doc0.replace(3, 4, fromString("", NULL, NULL, segOps)).exec((doc1, ch, pch) -> {
      var removed = pch.getRemoved();
      var added = pch.getAdded();
      assertEquals(2, removed.size());
      // var p = new Paragraph<Void, String, Void>(NULL, segOps, segOps.create("som"), NULL);
      assertEquals(new Paragraph<>(NULL, segOps, "Foo", NULL), removed.get(0));
      assertEquals(new Paragraph<>(NULL, segOps, "Bar", NULL), removed.get(1));
      assertEquals(1, added.size());
      assertEquals(new Paragraph<>(NULL, segOps, "FooBar", NULL), added.get(0));
    });
  }

  @Test
  void testRestyle() {
    // texts
    final String fooBar = "Foo Bar";
    final String and = " and ";
    final String helloWorld = "Hello World";
    // styles
    final String bold = "bold";
    final String empty = "";
    final String italic = "italic";

    var segOps = SegmentOps.<String>styledTextOps();

    var doc0 = new SimpleEditableStyledDocument<>("", "");

    BiConsumer<String, String> appendStyledText = (text, style) -> {
      var rosDoc = fromString(text, "", style, segOps);
      doc0.replace(doc0.getLength(), doc0.getLength(), rosDoc);
    };

    appendStyledText.accept(fooBar, bold);
    appendStyledText.accept(and, empty);
    appendStyledText.accept(helloWorld, bold);

    var styles = doc0.getStyleSpans(4, 17);
    assertEquals(styles.getSpanCount(), 3, "Invalid number of Spans");

    var newStyles = styles.mapStyles(style -> italic);
    doc0.setStyleSpans(4, newStyles);

    // assert the new segment structure:
    //  StyledText[text="Foo ", style=bold]
    //  StyledText[text="Bar and Hello", style=italic]
    //  StyledText[text=" World", style=bold]
    StyleSpans<String> spans = doc0.getParagraphs().get(0).getStyleSpans();
    assertEquals(spans.getSpanCount(), 3);
    assertEquals(spans.getStyleSpan(0).getStyle(), bold);
    assertEquals(spans.getStyleSpan(1).getStyle(), italic);
    assertEquals(spans.getStyleSpan(2).getStyle(), bold);
  }

}
