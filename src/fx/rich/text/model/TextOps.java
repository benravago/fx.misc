package fx.rich.text.model;

import fx.util.Either;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Extends {@link SegmentOps} by adding {@link #create(String)}, which can create a {@link SEG} segment from a given
 * {@link String}
 *
 * @param <SEG> the type of segment
 * @param <S> the type of style
 */
public interface TextOps<SEG, S> extends SegmentOps<SEG, S> {

  /**
   * Creates a segment using the given text. One could think of this as a mapping function from {@link String} to
   * {@link SEG}
   */
  public SEG create(String text);

  /**
   * Same as {@link SegmentOps#either(SegmentOps, SegmentOps, BiFunction)}, except that
   * {@link TextOps#create(String)} will use this object's {@code create(String)} method, not {@code rOps}' version.
   */
  public default <R> TextOps<Either<SEG, R>, S> _or(SegmentOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    return eitherL(this, rOps, mergeStyle);
  }

  /**
   * Same as {@link SegmentOps#either(SegmentOps, SegmentOps, BiFunction)}, except that
   * {@link TextOps#create(String)} will use {@code lOps}' {@code create(String)} method, not {@code rOps}' version.
   */
  public static <L, R, S> TextOps<Either<L, R>, S> eitherL(TextOps<L, S> lOps, SegmentOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    return new TextOpsLeft<>(lOps, rOps, mergeStyle);
  }

  /**
   * Same as {@link SegmentOps#either(SegmentOps, SegmentOps, BiFunction)}, except that
   * {@link TextOps#create(String)} will use {@code rOps}' {@code create(String)} method, not {@code lOps}' version.
   */
  public static <L, R, S> TextOps<Either<L, R>, S> eitherR(SegmentOps<L, S> lOps, TextOps<R, S> rOps, BiFunction<S, S, Optional<S>> mergeStyle) {
    return new TextOpsRight<>(lOps, rOps, mergeStyle);
  }

}