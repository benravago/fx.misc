package fx.rich.text.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class ReadOnlyStyledDocumentBuilderTest {

  static final TextOps<String, String> SEGMENT_OPS = SegmentOps.styledTextOps();

  @Test
  void adding_single_segment_single_style_single_paragraph_works() {
    var text = "a";
    var paragraphStyle = "ps style";
    var textStyle = "seg style";

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraph(text, textStyle));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(text, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void adding_multiple_styled_segment_single_paragraph_works() {
    var text = "a";
    var textStyle = "a style";
    var word = "word";
    var wordStyle = "word style";
    var paragraphStyle = "ps style";

    var styledSegList = new ArrayList<StyledSegment<String, String>>(2);
    styledSegList.addAll(Arrays.asList(new StyledSegment<>(text, textStyle), new StyledSegment<>(word, wordStyle)));

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraph(styledSegList));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(1, p.getSegments().size());
    assertEquals(text + word, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void adding_single_segment_single_styleSpans_single_paragraph_works() {
    var text = "a";
    var paragraphStyle = "ps style";
    var textStyle = "seg style";

    var spans = StyleSpans.singleton(textStyle, text.length());

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraph(text, spans));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(text, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void adding_single_segment_list_single_styleSpans_single_paragraph_works() {
    var text = "a";
    var paragraphStyle = "ps style";
    var textStyle = "seg style";

    var segmentList = Collections.singletonList(text);
    var spans = StyleSpans.singleton(textStyle, text.length());

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraph(segmentList, spans));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(text, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void adding_single_segment_single_style_without_par_style_paragraph_list_works() {
    var text = "a";
    var paragraphStyle = "ps style";
    var textStyle = "seg style";

    var singletonList = Collections.singletonList(Collections.singletonList(text));
    var spans = StyleSpans.singleton(textStyle, text.length());

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraphs(singletonList, spans));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(text, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void adding_single_segment_single_style_with_par_style_paragraph_list_works() {
    var text = "a";
    var paragraphStyle = "ps style";
    var textStyle = "seg style";

    var singletonList = Collections.singletonList(
      new Pair<>(paragraphStyle, Collections.singletonList(text))
    );
    var spans = StyleSpans.singleton(textStyle, text.length());

    var rosd = ReadOnlyStyledDocumentBuilder.constructDocument(
      SEGMENT_OPS, paragraphStyle, builder -> builder.addParagraphs0(singletonList, spans));

    assertEquals(1, rosd.getParagraphCount());

    var p = rosd.getParagraph(0);
    assertEquals(text, p.getText());
    assertEquals(textStyle, p.getStyleSpans().getStyleSpan(0).getStyle());
    assertEquals(paragraphStyle, p.getParagraphStyle());
  }

  @Test
  void attempting_to_build_ReadOnlyStyledDocument_using_empty_paragraph_list_throws_exception() {
    var builder = new ReadOnlyStyledDocumentBuilder<>(SEGMENT_OPS, "ps style");
    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    }, "Cannot build a ReadOnlyStyledDocument with an empty list of paragraphs");
  }

  @Test
  void using_a_builder_more_than_once_throws_exception() {
    var builder = new ReadOnlyStyledDocumentBuilder<>(SEGMENT_OPS, "ps style");
    builder.addParagraph("text", "text style");
    builder.build();
    assertThrows(IllegalStateException.class, () -> {
      builder.build();
    }, "Cannot use a single ReadOnlyStyledDocumentBuilder to build multiple ROSDs.");
  }

  @Test
  void creating_paragraph_with_different_segment_and_style_length_throws_exception() {
    var text = "a";
    int textStyle = 1;

    var singletonList = Collections.singletonList(Collections.singletonList(text));
    int segmentLength = text.length();
    int spanLength = segmentLength + 10;
    var spans = StyleSpans.singleton(textStyle, spanLength);

    var builder = new ReadOnlyStyledDocumentBuilder<Integer, String, Integer>(SegmentOps.styledTextOps(), 0);

    assertTrue(segmentLength != spanLength);

    assertThrows(IllegalArgumentException.class, () -> {
      builder.addParagraphs(singletonList, spans);
    }, "Style spans length must equal the length of all segments");
  }

}
