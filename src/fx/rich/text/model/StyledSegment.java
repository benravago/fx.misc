package fx.rich.text.model;

import java.util.Objects;

/**
 * Essentially, an immutable tuple that combines a {@link SEG segment} object and a
 * {@link S style} object together.
 *
 * @param <SEG> the segment type
 * @param <S> the style type
 */
public final class StyledSegment<SEG, S> {

  final SEG segment;

  public final SEG getSegment() {
    return segment;
  }

  final S style;

  public final S getStyle() {
    return style;
  }

  public StyledSegment(SEG segment, S style) {
    this.segment = segment;
    this.style = style;
  }

  @Override
  public String toString() {
    return String.format("StyledSegment(segment=%s style=%s)", segment, style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(segment, style);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof StyledSegment<?, ?> that)
      ? Objects.equals(this.segment, that.segment)
        && Objects.equals(this.style, that.style)
      : false;
  }

}
