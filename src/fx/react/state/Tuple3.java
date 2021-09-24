package fx.react.state;

public class Tuple3<A, B, C> {
  public final A _1;
  public final B _2;
  public final C _3;

  public Tuple3(A a, B b, C c) {
    _1 = a;
    _2 = b;
    _3 = c;
  }

  public A get1() { return _1; }
  public B get2() { return _2; }
  public C get3() { return _3; }

  public interface f3<A, B, C, R> { R apply(A a, B b, C c); }
  public interface c3<A, B, C> { void accept(A a, B b, C c); }

  public <T> T map(f3<? super A, ? super B, ? super C, ? extends T> f) {
    return f.apply(_1, _2, _3);
  }
  public void exec(c3<? super A, ? super B, ? super C> f) {
    f.accept(_1, _2, _3);
  }
}
