package fx.rich.text.model;

class SingletonSpans<S> extends StyleSpansBase<S> {

  final StyleSpan<S> span;

  SingletonSpans(StyleSpan<S> span) {
    this.span = span;
  }

  @Override
  public int length() {
    return span.getLength();
  }

  @Override
  public int getSpanCount() {
    return 1;
  }

  @Override
  public StyleSpan<S> getStyleSpan(int index) {
    if (index == 0) {
      return span;
    } else {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
  }

}