package fx.rich.text.model;

class PrependedSpans<S> extends StyleSpansBase<S> {

  final StyleSpans<S> original;
  final StyleSpan<S> prepended;

  int length = -1;
  int spanCount = -1;

  PrependedSpans(StyleSpans<S> original, StyleSpan<S> prepended) {
    this.original = original;
    this.prepended = prepended;
  }

  @Override
  public int length() {
    if (length == -1) {
      length = prepended.getLength() + original.length();
    }
    return length;
  }

  @Override
  public int getSpanCount() {
    if (spanCount == -1) {
      spanCount = 1 + original.getSpanCount();
    }
    return spanCount;
  }

  @Override
  public StyleSpan<S> getStyleSpan(int index) {
    return (index == 0) ? prepended : original.getStyleSpan(index - 1);
  }

}