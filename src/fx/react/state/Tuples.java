package fx.react.state;

public class Tuples {
  public static <A, B> Tuple2<A, B> t(A a, B b) {
    return new Tuple2<>(a, b);
  }
  public static <A, B, C> Tuple3<A, B, C> t(A a, B b, C c) {
    return new Tuple3<>(a, b, c);
  }
}