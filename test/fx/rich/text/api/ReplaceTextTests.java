package fx.rich.text.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import fx.text.junit.TestCase;

class ReplaceTextTests extends TestCase {

  @Test
  void replaceText_does_not_cause_index_out_of_bounds_exception() {
    r.interact(() -> {
      var test = "abc\n";
      area.replaceText(test);
      area.insertText(0, test + "def\n");

      // An IndexOutOfBoundsException occurs when trimming MaterializedListModification
      // in GenericEditableStyledDocumentBase.ParagraphList.observeInputs()
      area.replaceText(test);
      assertEquals(test, area.getText());

      area.clear();
      area.replaceText(test);
      area.appendText(test + "def");

      // An IndexOutOfBoundsException occurs when trimming MaterializedListModification
      // in GenericEditableStyledDocumentBase.ParagraphList.observeInputs()
      area.replaceText(test + "def");
      assertEquals(test + "def", area.getText());
    });

  }

  @Test
  void deselect_before_replaceText_does_not_cause_index_out_of_bounds_exception() {
    r.interact(() -> {
      area.replaceText("1234567890\n\nabcdefghij\n\n1234567890\n\nabcdefghij");

      // Select last line of text
      area.requestFollowCaret();
      area.selectLine();

      // Calling deselect, primes an IndexOutOfBoundsException to be thrown after replaceText
      area.deselect();

      // An internal IndexOutOfBoundsException may occur in ParagraphText.getRangeShapeSafely
      area.replaceText("1234567890\n\nabcdefghijklmno\n\n1234567890\n\nabcde");

      // This would fail if an exception occurred during ParagraphText.layoutChildren:updateSelectionShape()
      area.selectLine();
    });

  }

  @Test
  void previous_selection_before_replaceText_does_not_cause_index_out_of_bounds_exception() {
    r.interact(() -> {
      // For this test to work the area MUST be at the end of the document
      area.requestFollowCaret();

      // First text supplied by bug reporter: has 9 paragraphs, 344 characters
      area.replaceText(getTextA());

      // Any text can be selected anywhere in the document, this primed the exception
      area.selectWord();

      // Second text supplied by bug reporter: has 9 paragraphs, 344 characters, and contains two � characters
      area.replaceText(getTextB());

      // An internal IndexOutOfBoundsException may occur in ParagraphText.getRangeShapeSafely
      area.replaceText(getTextA());

      // This would fail if an exception occurred during ParagraphText.layoutChildren:updateSelectionShape()
      area.selectLine();
    });
  }

  // Reduced text supplied by bug reporter: has 9 paragraphs, 344 characters
  String getTextA() {
    return
      "<!DOCTYPE HTML>\n" + "\n" +
      "    Parempaa kuvaa ja &auml;&auml;nentoistoa jo vuodesta 1981 - HifiStudio</title>\n" + "\n" +
      "<meta property=\"og:title\" content=\"HifiStudio - Parempaa kuvaa ja &auml;&auml;nentoistoa jo vuodesta 1981\" />\n" + "\n" +
      "<meta property=\"og:url\" content=\"https://www.hifistudio.fi/fi/\" />\n" + "\n" +
      "                            <li><a href=\"/fi/tuotteet/muut-hifil";
  }

  // Reduced text supplied by bug reporter: has 9 paragraphs, 344 characters, and contains two � characters
  String getTextB() {
    return
      "<!DOCTYPE HTML>\n" + "\n" + "    SEIN�TELINEET - HifiStudio</title>\n" + "\n" +
      "<meta property=\"og:title\" content=\"HifiStudio - SEIN�TELINEET\" />\n" + "\n" +
      "<meta property=\"og:url\" content=\"https://www.hifistudio.fi/fi/tuotteet/laitetelineet/seinatelineet/91052\" />\n" + "\n" +
      "                            <li><a href=\"/fi/tuotteet/muut-hifilaitteet/cd-soittimet/15035\" class=\"top-product";
  }

}