package fx.rich.text.model;

import static fx.rich.text.model.TwoDimensional.Bias.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.IndexRange;

/**
 * An object (document) that is a list of styleable {@link Paragraph} that each contain a list of styleable segments.
 *
 * @param <PS> The type of the paragraph style.
 * @param <SEG> The type of the segments in the paragraph (e.g. {@link String}).
 * @param <S> The type of the style of individual segments.
 */
public interface StyledDocument<PS, SEG, S> extends TwoDimensional {

  int length();

  String getText();

  List<Paragraph<PS, SEG, S>> getParagraphs();

  StyledDocument<PS, SEG, S> concat(StyledDocument<PS, SEG, S> that);

  StyledDocument<PS, SEG, S> subSequence(int start, int end);

  default String getText(IndexRange range) {
    return getText(range.getStart(), range.getEnd());
  }

  default String getText(int start, int end) {
    return subSequence(start, end).getText();
  }

  default String getText(int paragraphIndex) {
    return getParagraph(paragraphIndex).getText();
  }

  default Paragraph<PS, SEG, S> getParagraph(int index) {
    return getParagraphs().get(index);
  }

  default int getParagraphLength(int paragraphIndex) {
    return getParagraph(paragraphIndex).length();
  }

  default StyledDocument<PS, SEG, S> subSequence(IndexRange range) {
    return subSequence(range.getStart(), range.getEnd());
  }

  default StyledDocument<PS, SEG, S> subDocument(int paragraphIndex) {
    return new ReadOnlyStyledDocument<>(Collections.singletonList(getParagraphs().get(paragraphIndex)));
  }

  default char charAt(int index) {
    var pos = offsetToPosition(index, Forward);
    return getParagraphs().get(pos.getMajor()).charAt(pos.getMinor());
  }

  default S getStyleOfChar(int index) {
    var pos2D = offsetToPosition(index, Forward);
    var paragraph = pos2D.getMajor();
    var col = pos2D.getMinor();
    return getParagraphs().get(paragraph).getStyleOfChar(col);
  }

  default S getStyleOfChar(int paragraph, int column) {
    return getParagraphs().get(paragraph).getStyleOfChar(column);
  }

  default S getStyleAtPosition(int position) {
    var pos2D = offsetToPosition(position, Forward);
    return getStyleAtPosition(pos2D.getMajor(), pos2D.getMinor());
  }

  default S getStyleAtPosition(int paragraph, int position) {
    return getParagraphs().get(paragraph).getStyleAtPosition(position);
  }

  default PS getParagraphStyle(int paragraph) {
    return getParagraphs().get(paragraph).getParagraphStyle();
  }

  default PS getParagraphStyleAtPosition(int position) {
    var pos = offsetToPosition(position, Forward);
    return getParagraphStyle(pos.getMajor());
  }

  default IndexRange getStyleRangeAtPosition(int position) {
    var pos2D = offsetToPosition(position, Forward);
    var paragraph = pos2D.getMajor();
    var col = pos2D.getMinor();
    return getParagraphs().get(paragraph).getStyleRangeAtPosition(col);
  }

  default IndexRange getStyleRangeAtPosition(int paragraph, int position) {
    return getParagraphs().get(paragraph).getStyleRangeAtPosition(position);
  }

  default StyleSpans<S> getStyleSpans(int from, int to) {
    var start = offsetToPosition(from, Forward);
    var end = to == from ? start : start.offsetBy(to - from, Backward);
    var startParIdx = start.getMajor();
    var endParIdx = end.getMajor();

    var affectedPars = endParIdx - startParIdx + 1;
    var subSpans = new ArrayList<StyleSpans<S>>(affectedPars);

    if (startParIdx == endParIdx) {
      var par = getParagraphs().get(startParIdx);
      subSpans.add(par.getStyleSpans(start.getMinor(), end.getMinor()));
    } else {
      var startPar = getParagraphs().get(startParIdx);
      subSpans.add(startPar.getStyleSpans(start.getMinor(), startPar.length() + 1));
      for (var i = startParIdx + 1; i < endParIdx; ++i) {
        var par = getParagraphs().get(i);
        subSpans.add(par.getStyleSpans(0, par.length() + 1));
      }
      var endPar = getParagraphs().get(endParIdx);
      subSpans.add(endPar.getStyleSpans(0, end.getMinor()));
    }

    var n = subSpans.stream().mapToInt(StyleSpans::getSpanCount).sum();
    var builder = new StyleSpansBuilder<S>(n);
    for (var spans : subSpans) {
      for (var span : spans) {
        builder.add(span);
      }
    }

    return builder.create();
  }

  default StyleSpans<S> getStyleSpans(int paragraph) {
    return getParagraphs().get(paragraph).getStyleSpans();
  }

  default StyleSpans<S> getStyleSpans(int paragraph, int from, int to) {
    return getParagraphs().get(paragraph).getStyleSpans(from, to);
  }

  /**
   * Returns the absolute position (i.e. the spot in-between characters) of the given column position in the given
   * paragraph.
   *
   * <p>For example, given a text with only one line {@code "text"} and a {@code columnPosition} value of {@code 1},
   * the value, {@code 1}, as in "position 1" would be returned:</p>
   * <pre>
   *  ┌ character index 0
   *  | ┌ character index 1
   *  | |   ┌ character index 3
   *  | |   |
   *  v v   v
   *
   * |t|e|x|t|
   *
   * ^ ^     ^
   * | |     |
   * | |     └ position 4
   * | └ position 1
   * └ position 0
   * </pre>
   *
   * <h3>Warning: Off-By-One errors can easily occur</h3>
   * <p>If the column index spans outside of the given paragraph's length, the returned value will
   * pass on to the previous/next paragraph. In other words, given a document with two paragraphs
   * (where the first paragraph's text is "some" and the second "thing"), then the following statements are true:</p>
   * <ul>
   *     <li><code>getAbsolutePosition(0, "some".length()) == 4 == getAbsolutePosition(1, -1)</code></li>
   *     <li><code>getAbsolutePosition(0, "some".length() + 1) == 5 == getAbsolutePosition(1, 0)</code></li>
   * </ul>
   *
   * @param paragraphIndex The index of the paragraph from which to start.
   * @param columnPosition If positive, the index going forward (the given paragraph's line or the next one(s)).
   *                       If negative, the index going backward (the previous paragraph's line(s))
   */
  default int getAbsolutePosition(int paragraphIndex, int columnPosition) {
    var position = position(paragraphIndex, columnPosition).toOffset();
    if (position < 0) {
      throw new IndexOutOfBoundsException(String.format("Negative index! Out of bounds by %s.", 0 - position));
    }
    if (length() < position) {
      throw new IndexOutOfBoundsException(String.format("Out of bounds by %s. Area Length: %s", position - length(), length()));
    }
    return position;
  }

}
