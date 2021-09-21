package fx.react;

@FunctionalInterface
public interface Guard extends AutoCloseable {

  static Guard EMPTY_GUARD = () -> {};

  /**
   * Releases this guard. Does not throw.
   */
  @Override
  void close();

  default Guard closeableOnce() {
    return new CloseableOnceGuard(this);
  }

  static Guard closeableOnce(Guard guard) {
    return guard.closeableOnce();
  }

  /**
   * Returns a guard that is a composition of multiple guards.
   * Its {@code close()} method closes the guards in reverse order.
   * @param guards guards that should be released (in reverse order)
   * when the returned guards is released.
   */
  static Guard multi(Guard... guards) {
    return switch (guards.length) {
      case 0 -> EMPTY_GUARD;
      case 1 -> guards[0];
      case 2 -> new BiGuard(guards[0], guards[1]);
      default -> new MultiGuard(guards);
    };
  }

}