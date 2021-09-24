package fx.rich.text.model;

class UpdatedSpans<S> extends StyleSpansBase<S> {

  final StyleSpans<S> original;
  final int index;
  final StyleSpan<S> update;

  int length = -1;

  UpdatedSpans(StyleSpans<S> original, int index, StyleSpan<S> update) {
    this.original = original;
    this.index = index;
    this.update = update;
  }

  @Override
  public int length() {
    if (length == -1) {
      length = original.length() - original.getStyleSpan(index).getLength() + update.getLength();
    }
    return length;
  }

  @Override
  public int getSpanCount() {
    return original.getSpanCount();
  }

  @Override
  public StyleSpan<S> getStyleSpan(int index) {
    return (index == this.index) ? update : original.getStyleSpan(index);
  }

}