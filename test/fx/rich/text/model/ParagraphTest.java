package fx.rich.text.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

class ParagraphTest {

  // Tests that when concatenating two paragraphs,
  // the style of the first one is used for the result.
  // This relates to merging text changes and issue #216.
  @Test
  void concatEmptyParagraphsTest() {
    var segOps = SegmentOps.<Boolean>styledTextOps();
    var p1 = new Paragraph<Void, String, Boolean>(null, segOps, segOps.create(""), true);
    var p2 = new Paragraph<Void, String, Boolean>(null, segOps, segOps.create(""), false);

    var p = p1.concat(p2);

    assertEquals(Boolean.TRUE, p.getStyleAtPosition(0));
  }

  // Relates to #345 and #505: calling `EditableStyledDocument::setStyleSpans`
  // when styling an empty paragraph would throw an exception.
  // Also relates to #449: where empty paragraphs were being skipped.
  @Test
  void restylingEmptyParagraphViaStyleSpansWorks() {
    var segOps = SegmentOps.<Boolean>styledTextOps();
    var p = new Paragraph<Void, String, Boolean>(null, segOps, segOps.createEmptySeg(), false);
    assertEquals(0, p.length());

    var builder = new StyleSpansBuilder<Boolean>();
    builder.add(true, 2);
    var spans = builder.create();
    var restyledP = p.restyle(0, spans);

    assertTrue(restyledP.getStyleSpans().styleStream().allMatch(b -> b));
  }

  // Relates to #696 (caused by #685, coming from #449) where an empty
  // StyleSpans being applied to an empty paragraph results in an Exception
  @Test
  void restylingEmptyParagraphViaEmptyStyleSpansWorks() {

    var test = Collections.singleton("test");
    var segOps = SegmentOps.<Collection<String>>styledTextOps();
    var p = new Paragraph<Void, String, Collection<String>>(null, segOps, "", test);
    assertEquals(0, p.length());

    var spans = new StyleSpans<Collection<String>>() {
      @Override public Position position(int major, int minor) { return null; }
      @Override public Position offsetToPosition(int offset, Bias bias) { return null; }
      @Override public StyleSpan<Collection<String>> getStyleSpan(int index) { return null; }
      @Override public int getSpanCount() { return 0; }
      @Override public int length() { return 0; }
    };

    var restyledP = p.restyle(0, spans);
    assertEquals(test, restyledP.getStyleSpans().getStyleSpan(0).getStyle());
  }

  // Relates to #815 where an undo after deleting a portion of styled text in a multi-
  // styled paragraph causes an exception in UndoManager receiving an unexpected change.
  @Test
  void multiStyleParagraphReturnsCorrect_subSequenceOfLength() {

    var test = Collections.singleton("test");
    var segOps = SegmentOps.<Collection<String>>styledTextOps();
    var ssb = new StyleSpansBuilder<Collection<String>>(2);
    ssb.add(Collections.emptyList(), 8);
    ssb.add(test, 8);

    var p = new Paragraph<Void, String, Collection<String>>(null, segOps, "noStyle hasStyle", ssb.create());
    assertEquals(test, p.subSequence(p.length()).getStyleOfChar(0));
  }

}
