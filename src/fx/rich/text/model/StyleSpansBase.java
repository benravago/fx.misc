package fx.rich.text.model;

abstract class StyleSpansBase<S> implements StyleSpans<S> {

  protected final TwoLevelNavigator navigator =
    new TwoLevelNavigator(this::getSpanCount, i -> getStyleSpan(i).getLength());

  @Override
  public Position position(int major, int minor) {
    return navigator.position(major, minor);
  }

  @Override
  public Position offsetToPosition(int offset, Bias bias) {
    return navigator.offsetToPosition(offset, bias);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof StyleSpans<?> that) {
      if (this.getSpanCount() != that.getSpanCount()) {
        return false;
      }
      for (var i = 0; i < this.getSpanCount(); ++i) {
        if (!this.getStyleSpan(i).equals(that.getStyleSpan(i))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    var result = 1;
    for (var span : this) {
      result = 31 * result + span.hashCode();
    }
    return result;
  }

}