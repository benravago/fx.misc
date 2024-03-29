package fx.rich.text;

import java.text.BreakIterator;
import java.util.Collection;

import fx.rich.text.model.EditableStyledDocument;
import javafx.beans.NamedArg;

/**
 * A convenience subclass of {@link StyleClassedTextArea} with fixed-width font and an undo manager that observes
 * only plain text changes (not styled changes). It's style class is {@code code-area}.
 */
public class CodeArea extends StyleClassedTextArea {

  /*<init>*/ {
    getStyleClass().add("code-area");
    // load the default style that defines a fixed-width font
    getStylesheets().add(CodeArea.class.getResource("code-area.css").toExternalForm());
    // don't apply preceding style to typed text
    setUseInitialStyleForInsertion(true);
  }

  /**
   * Creates an area that can render and edit the same {@link EditableStyledDocument} as another {@link CodeArea}.
   */
  public CodeArea(@NamedArg("document") EditableStyledDocument<Collection<String>, String, Collection<String>> document) {
    super(document, false);
  }

  /**
   * Creates an area with no text.
   */
  public CodeArea() {
    super(false);
  }

  /**
   * Creates a text area with initial text content.
   * Initial caret position is set at the beginning of text content.
   *
   * @param text Initial text content.
   */
  public CodeArea(@NamedArg("text") String text) {
    this();

    appendText(text);
    getUndoManager().forgetHistory();
    getUndoManager().mark();

    // position the caret at the beginning
    selectRange(0, 0);
  }

  @Override // to select words containing underscores
  public void selectWord() {
    if (getLength() == 0) {
      return;
    }
    var csb = getCaretSelectionBind();
    var paragraph = csb.getParagraphIndex();
    var position = csb.getColumnPosition();

    var paragraphText = getText(paragraph);
    var breakIterator = BreakIterator.getWordInstance(getLocale());
    breakIterator.setText(paragraphText);

    breakIterator.preceding(position);
    var start = breakIterator.current();

    while (start > 0 && paragraphText.charAt(start - 1) == '_') {
      if (--start > 0 && !breakIterator.isBoundary(start - 1)) {
        breakIterator.preceding(start);
        start = breakIterator.current();
      }
    }

    breakIterator.following(position);
    var end = breakIterator.current();
    var len = paragraphText.length();

    while (end < len && paragraphText.charAt(end) == '_') {
      if (++end < len && !breakIterator.isBoundary(end + 1)) {
        breakIterator.following(end);
        end = breakIterator.current();
      }
      // For some reason single digits aren't picked up so ....
      else if (Character.isDigit(paragraphText.charAt(end))) {
        end++;
      }
    }

    csb.selectRange(paragraph, start, paragraph, end);
  }

}
