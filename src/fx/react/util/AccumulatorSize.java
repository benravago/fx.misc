package fx.react.util;

public enum AccumulatorSize {
  ZERO, ONE, MANY;

  public static AccumulatorSize fromInt(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("Size cannot be negative: " + n);
    }
    return switch (n) {
      case 0 -> ZERO;
      case 1 -> ONE;
      default -> MANY;
    };
  }

}