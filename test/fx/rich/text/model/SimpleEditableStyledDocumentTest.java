package fx.rich.text.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javafx.scene.control.IndexRange;

class SimpleEditableStyledDocumentTest {

  final TextOps<String, String> segOps = SegmentOps.styledTextOps();

  /**
   * The style of the inserted text will be the style at position
   * {@code start} in the current document.
   */
  <PS> void replaceText(EditableStyledDocument<PS, String, String> doc, int start, int end, String text) {
    var styledDoc = ReadOnlyStyledDocument.fromString(
      text, doc.getParagraphStyleAtPosition(start), doc.getStyleAtPosition(start), segOps);
    doc.replace(start, end, styledDoc);
  }

  @Test
  void testConsistencyOfTextWithLength() {
    var document = new SimpleEditableStyledDocument<>("", "");
    document.getText(); // enforce evaluation of text property
    document.getLength(); // enforce evaluation of length property

    document.lengthProperty().addListener(obs -> {
      var length = document.getLength();
      var textLength = document.getText().length();
      assertEquals(length, textLength);
    });

    replaceText(document, 0, 0, "A");
  }

  @Test
  void testConsistencyOfLengthWithText() {
    var document = new SimpleEditableStyledDocument<>("", "");
    document.getText(); // enforce evaluation of text property
    document.getLength(); // enforce evaluation of length property

    document.textProperty().addListener(obs -> {
      var textLength = document.getText().length();
      var length = document.getLength();
      assertEquals(textLength, length);
    });

    replaceText(document, 0, 0, "A");
  }

  @Test
  void testUnixParagraphCount() {
    var document = new SimpleEditableStyledDocument<>("", "");
    var text = "X\nY";
    replaceText(document, 0, 0, text);
    assertEquals(2, document.getParagraphs().size());
  }

  @Test
  void testMacParagraphCount() {
    var document = new SimpleEditableStyledDocument<>("", "");
    var text = "X\rY";
    replaceText(document, 0, 0, text);
    assertEquals(2, document.getParagraphs().size());
  }

  @Test
  void testWinParagraphCount() {
    var document = new SimpleEditableStyledDocument<>("", "");
    var text = "X\r\nY";
    replaceText(document, 0, 0, text);
    assertEquals(2, document.getParagraphs().size());
  }

  @Test
  void testGetTextWithEndAfterNewline() {
    var doc = new SimpleEditableStyledDocument<>(true, "");

    replaceText(doc, 0, 0, "123\n");
    var txt1 = doc.getText(0, 4);
    assertEquals(4, txt1.length());

    replaceText(doc, 4, 4, "567");
    var txt2 = doc.getText(2, 4);
    assertEquals(2, txt2.length());

    replaceText(doc, 4, 4, "\n");
    var txt3 = doc.getText(2, 4);
    assertEquals(2, txt3.length());
  }

  @Test
  void testWinDocumentLength() {
    var document = new SimpleEditableStyledDocument<>("", "");
    replaceText(document, 0, 0, "X\r\nY");
    assertEquals(document.getText().length(), document.getLength());
  }

  @Test
  void testSetEmptyParagraphStyle() {
    var document = new SimpleEditableStyledDocument<>("", "");
    var newParStyle = "new style";
    document.setParagraphStyle(0, newParStyle);
    assertEquals(newParStyle, document.getParagraphStyle(0));
  }

  @Test
  void testSetNonEmptyParagraphStyle() {
    var document = new SimpleEditableStyledDocument<>("", "");
    replaceText(document, 0, 0, "some text");
    var newParStyle = "new style";
    document.setParagraphStyle(0, newParStyle);
    assertEquals(newParStyle, document.getParagraphStyle(0));
  }

  @Test
  void testGetStyleRangeAtPosition() {
    var document = new SimpleEditableStyledDocument<>("", "");
    var first = "some";
    var second = " text";
    replaceText(document, 0, 0, first + second);
    document.setStyle(0, first.length(), "abc");

    var range = document.getStyleRangeAtPosition(0);
    var expected = new IndexRange(0, first.length());
    assertEquals(expected, range);

    range = document.getStyleRangeAtPosition(first.length());
    assertEquals(expected, range);

    range = document.getStyleRangeAtPosition(first.length() + 1);
    expected = new IndexRange(first.length(), (first + second).length());
    assertEquals(expected, range);
  }

}
