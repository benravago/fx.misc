package fx.rich.text.model;

import java.util.Objects;

/**
 * Essentially, a tuple of a given style {@link S} that spans a given length.
 *
 * @param <S> the style type
 */
public class StyleSpan<S> {

  final S style;
  final int length;

  /**
   * Creates a style span. Note: length cannot be negative.
   */
  public StyleSpan(S style, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("StyleSpan's length cannot be negative");
    }
    this.style = style;
    this.length = length;
  }

  public S getStyle() {
    return style;
  }

  public int getLength() {
    return length;
  }

  /**
   * Two {@code StyleSpan}s are considered equal if they have equal length and
   * equal style.
   */
  @Override
  public boolean equals(Object other) {
    return (other instanceof StyleSpan<?> that)
      ? this.length == that.length
        && Objects.equals(this.style, that.style)
      : false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(style, length);
  }

  @Override
  public String toString() {
    return String.format("StyleSpan[length=%s, style=%s]", length, style);
  }

}
