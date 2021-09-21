package fx.layout.flow;

/**
 * Helper class: stores an {@link #offset} value, which should either be offset from the start if {@link #fromStart}
 * is true or from the end if false.
 */
class Offset {

  static Offset fromStart(double offset) {
    return new Offset(offset, true);
  }

  static Offset fromEnd(double offset) {
    return new Offset(offset, false);
  }

  final double offset;
  final boolean fromStart;

  Offset(double offset, boolean fromStart) {
    this.offset = offset;
    this.fromStart = fromStart;
  }

  double getValue() {
    return offset;
  }

  boolean isFromStart() {
    return fromStart;
  }

  boolean isFromEnd() {
    return !fromStart;
  }

  Offset add(double delta) {
    return new Offset(offset + delta, fromStart);
  }

}
