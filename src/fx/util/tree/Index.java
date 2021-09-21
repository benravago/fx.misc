package fx.util.tree;

import java.util.function.BiFunction;

public final class Index {

  public final int major;
  public final int minor;

  public Index(int major, int minor) {
    this.major = major;
    this.minor = minor;
  }

  public <T> T map(BiFunction<Integer, Integer, T> f) {
    return f.apply(major, minor);
  }

  public Index major(int adjustment) {
    return new Index(major + adjustment, minor);
  }

  public Index minor(int adjustment) {
    return new Index(major, minor + adjustment);
  }

  @Override
  public String toString() {
    return "[" + major + ", " + minor + "]";
  }
}