package fx.rich.text.model;

import java.util.ArrayList;

class SubSpans<S> extends StyleSpansBase<S> {

  final StyleSpans<S> original;
  final int firstIdxInOrig;
  final int spanCount;
  final StyleSpan<S> firstSpan;
  final StyleSpan<S> lastSpan;

  int length = -1;

  SubSpans(StyleSpans<S> original, Position from, Position to) {
    this.original = original;
    this.firstIdxInOrig = from.getMajor();
    this.spanCount = to.getMajor() - from.getMajor() + 1;

    if (spanCount == 1) {
      var span = original.getStyleSpan(firstIdxInOrig);
      var len = to.getMinor() - from.getMinor();
      firstSpan = lastSpan = new StyleSpan<>(span.getStyle(), len);
    } else {
      var startSpan = original.getStyleSpan(firstIdxInOrig);
      int len = startSpan.getLength() - from.getMinor();
      firstSpan = new StyleSpan<>(startSpan.getStyle(), len);

      var endSpan = original.getStyleSpan(to.getMajor());
      lastSpan = new StyleSpan<>(endSpan.getStyle(), to.getMinor());
    }
  }

  @Override
  public int length() {
    if (length == -1) {
      length = 0;
      for (var span : this) {
        length += span.getLength();
      }
    }
    return length;
  }

  @Override
  public int getSpanCount() {
    return spanCount;
  }

  @Override
  public StyleSpan<S> getStyleSpan(int index) {
    if (index == 0) {
      return firstSpan;
    } else if (index == spanCount - 1) {
      return lastSpan;
    } else if (index < 0 || index >= spanCount) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    } else {
      return original.getStyleSpan(firstIdxInOrig + index);
    }
  }

  @Override
  public String toString() {
    var spans = new ArrayList<StyleSpan<S>>(spanCount);
    for (var i = 0; i < spanCount; i++) {
      spans.add(getStyleSpan(i));
    }
    return "SubSpans(length=" + length + " spanCount=" + getSpanCount() + " spans=" + spans + ')';
  }

}