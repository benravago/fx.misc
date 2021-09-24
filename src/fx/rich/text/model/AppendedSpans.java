package fx.rich.text.model;

class AppendedSpans<S> extends StyleSpansBase<S> {

  final StyleSpans<S> original;
  final StyleSpan<S> appended;

  int length = -1;
  int spanCount = -1;

  AppendedSpans(StyleSpans<S> original, StyleSpan<S> appended) {
    this.original = original;
    this.appended = appended;
  }

  @Override
  public int length() {
    if (length == -1) {
      length = original.length() + appended.getLength();
    }
    return length;
  }

  @Override
  public int getSpanCount() {
    if (spanCount == -1) {
      spanCount = original.getSpanCount() + 1;
    }
    return spanCount;
  }

  @Override
  public StyleSpan<S> getStyleSpan(int index) {
    return (index == getSpanCount() - 1) ? appended : original.getStyleSpan(index);
  }

}