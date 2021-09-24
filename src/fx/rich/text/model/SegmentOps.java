package fx.rich.text.model;

import java.util.Optional;
import java.util.function.BiFunction;

import fx.util.Either;

/**
 * Defines the operations which are supported on a specific segment type.
 *
 * @param <SEG> The segment type
 * @param <S> The style type for the segment
 */
public interface SegmentOps<SEG, S> {

  int length(SEG seg);

  char charAt(SEG seg, int index);

  String getText(SEG seg);

  SEG subSequence(SEG seg, int start, int end);

  SEG subSequence(SEG seg, int start);

  /**
   * Joins two consecutive segments together into one or {@link Optional#empty()} if they cannot be joined.
   */
  Optional<SEG> joinSeg(SEG currentSeg, SEG nextSeg);

  /**
   * Joins two consecutive styles together into one or {@link Optional#empty()} if they cannot be joined. By default,
   * returns {@link Optional#empty()}.
   */
  default Optional<S> joinStyle(S currentStyle, S nextStyle) {
    return Optional.empty();
  }

  /**
   * Creates an empty segment. This method should return the same object for better performance and memory usage.
   */
  SEG createEmptySeg();

  /**
   * Creates a {@link TextOps} specified for a {@link String} segment that never merges consecutive styles
   */
  static <S> TextOps<String, S> styledTextOps() {
    return styledTextOps((s1, s2) -> Optional.empty());
  }

  /**
   * Creates a {@link TextOps} specified for a {@link String}
   */
  static <S> TextOps<String, S> styledTextOps(BiFunction<S, S, Optional<S>> mergeStyle) {
    return new TextOpsBase<String, S>("") {
      @Override
      public char realCharAt(String s, int index) {
        return s.charAt(index);
      }
      @Override
      public String realGetText(String s) {
        return s;
      }
      @Override
      public String realSubSequence(String s, int start, int end) {
        return s.substring(start, end);
      }
      @Override
      public String create(String text) {
        return text;
      }
      @Override
      public int length(String s) {
        return s.length();
      }
      @Override
      public Optional<String> joinSeg(String currentSeg, String nextSeg) {
        return Optional.of(currentSeg + nextSeg);
      }
      @Override
      public Optional<S> joinStyle(S currentStyle, S nextStyle) {
        return mergeStyle.apply(currentStyle, nextStyle);
      }
    };
  }

  /**
   * Returns a {@link SegmentOps} that specifies its segment type to be an {@link Either}
   * whose {@link Either#left(Object) left} value is this segment type and
   * whose {@link Either#right(Object) right} value is {@code rOps}' segment type.
   */
  default <R> SegmentOps<Either<SEG, R>, S> or(SegmentOps<R, S> rOps) {
    return either(this, rOps);
  }

  /**
   * Returns a {@link SegmentOps}
   *  that specifies its segment type to be an {@link Either}
   *      whose {@link Either#left(Object) left} value is this segment type and
   *      whose {@link Either#right(Object) right} value is {@code rOps}' segment type, and
   *  that specifies its style type to be {@link Either}
   *      whose {@link Either#left(Object) left} value is this style type and
   *      whose {@link Either#right(Object) right} value is {@code rOps}' style type.
   */
  default <RSeg, RStyle> SegmentOps<Either<SEG, RSeg>, Either<S, RStyle>> orStyled(SegmentOps<RSeg, RStyle> rOps) {
    return eitherStyles(this, rOps);
  }

  /**
   * Returns a {@link SegmentOps}
   *  that specifies its segment type to be an {@link Either}
   *      whose {@link Either#left(Object) left} value is {@code lOps}' segment type and
   *      whose {@link Either#right(Object) right} value is {@code rOps}' segment type, and
   *  that specifies its style type to be {@link Either}
   *      whose {@link Either#left(Object) left} value is {@code lOps}' style type and
   *      whose {@link Either#right(Object) right} value is {@code rOps}' style type.
   *
   * Note: consecutive styles will not be merged.
   */
  static <LSeg, LStyle, RSeg, RStyle> SegmentOps<Either<LSeg, RSeg>, Either<LStyle, RStyle>> eitherStyles(SegmentOps<LSeg, LStyle> lOps, SegmentOps<RSeg, RStyle> rOps) {
    return new SegmentOpsStyled<>(lOps, rOps);
  }

  /**
   * Returns a {@link SegmentOps} that specifies its segment type to be an {@link Either}
   * whose {@link Either#left(Object) left} value is {@code lOps}' segment type and
   * whose {@link Either#right(Object) right} value is {@code rOps}' segment type.
   *
   * Note: consecutive styles will not be merged.
   */
  static <LSeg, RSeg, Style> SegmentOps<Either<LSeg, RSeg>, Style> either(SegmentOps<LSeg, Style> lOps, SegmentOps<RSeg, Style> rOps) {
    return either(lOps, rOps, (leftStyle, rightStyle) -> Optional.empty());
  }

  /**
   * Returns a {@link SegmentOps} that specifies its segment type to be an {@link Either}
   * whose {@link Either#left(Object) left} value is {@code lOps}' segment type and
   * whose {@link Either#right(Object) right} value is {@code rOps}' segment type.
   */
  static <LSeg, RSeg, Style> SegmentOps<Either<LSeg, RSeg>, Style> either(SegmentOps<LSeg, Style> lOps, SegmentOps<RSeg, Style> rOps, BiFunction<Style, Style, Optional<Style>> mergeStyle) {
    return new SegmentOpsEither<>(lOps, rOps, mergeStyle);
  }

}